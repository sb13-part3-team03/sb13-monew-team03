package com.codeit.monew.comment.repository.querydsl;


import com.codeit.monew.article.entity.QArticle;
import com.codeit.monew.comment.dto.command.CommentDtoCreateCommand;
import com.codeit.monew.comment.dto.command.CommentQueryCommand;
import com.codeit.monew.comment.entity.QComment;
import com.codeit.monew.comment.entity.QCommentLike;
import com.codeit.monew.user.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
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
                        article.id,
                        user.id,
                        user.nickname,
                        comment.content,
                        JPAExpressions
                                .select(commentLike.id.count())
                                .from(commentLike)
                                .where(commentLike.comment.eq(comment)),
                        JPAExpressions
                                .selectOne()
                                .from(commentLike)
                                .where(
                                        commentLike.comment.eq(comment),
                                        commentLike.user.eq(user)
                                )
                                .exists(),
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

        return null;
    }
}
