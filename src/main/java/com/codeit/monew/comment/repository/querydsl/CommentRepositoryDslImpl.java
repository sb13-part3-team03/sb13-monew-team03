package com.codeit.monew.comment.repository.querydsl;


import com.codeit.monew.article.entity.QArticle;
import com.codeit.monew.comment.dto.command.CommentDtoCreateCommand;
import com.codeit.monew.comment.dto.command.CommentQueryCommand;
import com.codeit.monew.comment.entity.QComment;
import com.codeit.monew.comment.entity.QCommentLike;
import com.codeit.monew.comment.exception.CommentException;
import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.user.entity.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class CommentRepositoryDslImpl implements CommentRepositoryDsl {

    private final JPAQueryFactory queryFactory;

    private final QComment comment = QComment.comment;
    private final QCommentLike commentLike = QCommentLike.commentLike;
    private final QUser user = QUser.user;
    private final QArticle article = QArticle.article;

    @Override
    public Optional<CommentDtoCreateCommand> getDtoCommandById(UUID commentId){
        /*
          select
            c,
            u,
            a,
            count(cl.id),
            exists(
                select 1
                from count_likes my_cl
                where my_cl.comment_id = c.id
                and my_cl.user_id = u.id
            )
            from comments c
            join users u
                on u.id = c.user_id
            join article a
                on a.id = c.article_id
            left join
                comment_likes cl
                on c.id = cl.comment_id
             where c.id = u.id
             group by u.id
         */
        CommentDtoCreateCommand result = queryFactory.select(
                Projections.constructor(
                        CommentDtoCreateCommand.class,
                        comment.id,
                        comment.article.id,
                        user.id,
                        user.nickname,
                        comment.content,
                        likeCount(),
                        likeByMe(commentLike.user.eq(user)),
                        comment.createdAt
                ))
                .from(comment)
                .join(comment.user,user)
                .where(comment.id.eq(commentId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Slice<CommentDtoCreateCommand> getAllCommentsWithCursor(CommentQueryCommand command){
        /*  ANSI SQL
         select
            c.id,
            a.id,
            u.id,
            u.nickname,
            c.content,
            count( cl.id ) as lc,
            exists(
                select 1
                from comment_likes mcl
                where mcl.user_id = $requestedUserId
                    and mcl.comment_id = c.id
            )
        from comments c
            join users u
                on u.id = c.user_id
            join articles a
                on a.id = c.article_id
            left join comment_likes cl
                on cl.comment_id = c.id
        where c.article_id = $articleId [or all article = no where command]
            [and (c.created_at or lc) (> or <) $cursor]
            [and c.created_at (> or <) $after]
        group by
            c.id,
            a.id,
            u.id,
            u.nickname,
            c.content,
            c.created_at
        order by (c.created_at or lc) $direction
        fetch first $size rows only
         */
        int pageSize = command.size().intValue();

        // query for select, from
        JPAQuery<CommentDtoCreateCommand> query = queryFactory
                .select(
                        Projections.constructor(
                                CommentDtoCreateCommand.class,
                                comment.id,
                                article.id,
                                user.id,
                                user.nickname,
                                comment.content,
                                likeCount(),
                                likeByMe(commentLike.user.id.eq(command.requestUserId())),
                                comment.createdAt
                        )
                )
                .from(comment)
                .join(comment.user,user)
                .join(comment.article,article)
                .where(getCursorCondition(command))
                .orderBy(
                        getOrderCondition(
                                command.orderBy(),
                                command.direction()
                        )
                )
                .limit(pageSize + 1);

        // get data from db
        List<CommentDtoCreateCommand> result = query.fetch();

        // has next set
        boolean hasNext = result.size() > pageSize;
        if (hasNext) result.remove(result.size() - 1);


        return new SliceImpl<>(result,PageRequest.of(0,pageSize),hasNext);
    }


    /*
    getting JPAQuery Object method.
     */

    private BooleanExpression likeByMe(BooleanExpression expression){
        return JPAExpressions
                .selectOne()
                .from(commentLike)
                .where(
                        commentLike.comment.eq(comment),
                        expression
                )
                .exists();
    }

    private JPQLQuery<Long> likeCount(){
        return JPAExpressions
                .select(commentLike.id.count())
                .from(commentLike)
                .where(commentLike.comment.eq(comment));
    }

    private NumberExpression<Long> likeCountExpression(){
        return Expressions.numberTemplate(
                Long.class,
                "({0})",
                likeCount() // getting likeCount query
        );
    }

    /*
    "where()" command query builder method.
     */

    private BooleanBuilder getCursorCondition(
           CommentQueryCommand command
    ){
        BooleanBuilder where = new BooleanBuilder();

        log.debug("CommentsQuery - query condition from cursor");

        // set if articleId is not null, add condition.
        where.and(
                setArticleIdCondition(
                        command.articleId()
                )
        );

        // set if cursor is not null, add condition cursor query.
        // if null, set just order.
        where.and(
                setCursorCondition(
                        command.orderBy(),
                        command.cursor(),
                        command.after(),
                        command.direction()
                )
        );

        return where;
    }

    private BooleanExpression setArticleIdCondition(UUID articleId){

        log.debug("CommentsQuery - got article Id = {}", articleId);

        return articleId == null ? null : article.id.eq(articleId);
    }


    private BooleanExpression setCursorCondition(
            String orderBy,
            String cursor,
            String after,
            String direction
    ){

        log.debug("CommentsQuery - got orderBy = {}, cursor = {}", orderBy, cursor);

        return switch (orderBy.toUpperCase(Locale.ROOT)) {
            case "CREATEDAT" -> getConditionFilterWithCreatedAt(direction, cursor);
            case "LIKECOUNT" -> getConditionFilterWithLikeCount(direction, cursor, after);
            default -> {
                log.debug("orderBy value error - {}", orderBy);
                throw new CommentException(ErrorCode.COMMENT_INVALID_VALUE);
            }
        };
    }


    private BooleanExpression getConditionFilterWithCreatedAt(String direction, String cursor){
        try{
            // no where define with no  cursor
            if (cursor == null || cursor.isBlank()) return null;

            Instant createdAt = Instant.parse(cursor);

            return desc(direction) ? comment.createdAt.lt(createdAt) : comment.createdAt.gt(createdAt);
        } catch (DateTimeParseException e) {

            log.error("cursor value can not parse as {} - value = {}", Instant.class, cursor,e);

            throw new CommentException(ErrorCode.COMMENT_INVALID_VALUE);
        }
    }


    private BooleanExpression getConditionFilterWithLikeCount(String direction, String cursor, String after){
        try {
            // no where define with no  cursor
            if (cursor == null || cursor.isBlank()) return null;

            Long count = Long.parseLong(cursor);

            NumberExpression<Long> likeCount = likeCountExpression();

            // set sub cursor for check ctime at same like count.
            return desc(direction)
                    ? likeCount.lt(count).or(likeCount.eq(count).and(getConditionFilterWithCreatedAt(direction, after)))
                    : likeCount.gt(count).or(likeCount.eq(count).and(getConditionFilterWithCreatedAt(direction, after)));
        } catch (NumberFormatException e) {

            log.error("cursor value can not parse as {} - value = {}", Long.class, cursor,e);

            throw new CommentException(ErrorCode.COMMENT_INVALID_VALUE);
        }
    }


    private OrderSpecifier<?> getOrderCondition(String orderBy, String direction){
        return switch (orderBy.toUpperCase()){
            case "CREATEDAT" -> getOrderConditionWithCreatedAt(direction);
            case "LIKECOUNT" -> getOrderConditionWithLikeCount(direction);
            default -> {

                log.error("order value error - {}",orderBy);

                throw new CommentException(ErrorCode.COMMENT_INVALID_VALUE);
            }
        };
    }

    private OrderSpecifier<Instant> getOrderConditionWithCreatedAt(String direction){
        return desc(direction) ? comment.createdAt.desc() : comment.createdAt.asc();
    }

    private OrderSpecifier<?> getOrderConditionWithLikeCount(String direction){
        return desc(direction) ? likeCountExpression().desc() : likeCountExpression().asc();
    }

    /*
    sharable basic method
     */

    private boolean desc(String direction){
        return direction.equalsIgnoreCase("desc");
    }

}
