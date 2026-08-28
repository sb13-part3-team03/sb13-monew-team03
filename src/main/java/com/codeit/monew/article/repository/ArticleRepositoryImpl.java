package com.codeit.monew.article.repository;

import com.codeit.monew.article.dto.command.ArticleSearchCommand;
import com.codeit.monew.article.dto.response.ArticleSearchResultDto;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.article.entity.QArticleInterest;
import com.codeit.monew.article.entity.QArticleView;
import com.codeit.monew.comment.entity.QComment;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.codeit.monew.article.entity.QArticle.article;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 검색 조건에 해당하는 게시글의 전체 개수를 조회합니다.
     *
     * @param command 게시글 검색 조건
     * @return 검색 조건에 해당하는 게시글 수
     */
    @Override
    @Transactional(readOnly = true)
    public long countTotalElements(ArticleSearchCommand command) {

        Long count = queryFactory
                .select(article.id.count())
                .from(article)
                .where(
                        article.deletedAt.isNull(),
                        keywordContains(command.keyword()),
                        hasInterestExpression(command.interestId()),
                        sourceIn(command.sourceIn()),
                        publishedAtGoe(command.publishDateFrom()),
                        publishedAtLoe(command.publishDateTo())
                )
                .fetchOne();

        return count != null ? count : 0;
    }

    /**
     * 검색 조건에 따라 게시글을 조회하고 댓글 수와 조회 수를 집계합니다.
     *
     * @param command 게시글 검색 조건
     * @param orderBy 정규화된 정렬 기준
     * @return 검색 결과 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<ArticleSearchResultDto> searchArticles(ArticleSearchCommand command, String orderBy) {

        return queryFactory
                .select(Projections.constructor(
                        ArticleSearchResultDto.class,
                        article,
                        commentCountExpression(),
                        viewCountExpression(),
                        viewedByMeExpression(command.userId())
                ))
                .from(article)
                .where(
                        article.deletedAt.isNull(),
                        keywordContains(command.keyword()),
                        hasInterestExpression(command.interestId()),
                        sourceIn(command.sourceIn()),
                        publishedAtGoe(command.publishDateFrom()),
                        publishedAtLoe(command.publishDateTo()),
                        publishDateCursorCondition(
                                orderBy,
                                command.direction(),
                                command.cursor(),
                                command.after(),
                                command.afterId()
                        ),
                        cursorCondition(command, orderBy)
                )
                .orderBy(
                        orderSpecifiers(orderBy, command.direction())
                )
                .limit(command.limit() + 1)
                .fetch();

    }

    // 댓글 수 서브쿼리
    private NumberExpression<Long> commentCountExpression() {
        QComment commentCount = new QComment("commentCount");

        return Expressions.numberTemplate(
                Long.class,
                "({0})",
                JPAExpressions
                        .select(commentCount.id.count())
                        .from(commentCount)
                        .where(
                                commentCount.article.eq(article),
                                commentCount.deletedAt.isNull()
                        )
        );
    }

    // 조회 수 서브쿼리
    private NumberExpression<Long> viewCountExpression() {
        QArticleView viewCount = new QArticleView("viewCount");

        return Expressions.numberTemplate(
                Long.class,
                "({0})",
                JPAExpressions
                        .select(viewCount.id.count())
                        .from(viewCount)
                        .where(viewCount.article.eq(article))
        );
    }

    // 게시글의 관심사 존재 여부를 EXISTS로 확인
    private BooleanExpression hasInterestExpression(UUID interestId) {
        if (interestId == null) {
            return null;
        }

        QArticleInterest articleInterest = new QArticleInterest("articleInterest");

        return JPAExpressions
                .selectOne()
                .from(articleInterest)
                .where(
                        articleInterest.article.eq(article),
                        articleInterest.interest.id.eq(interestId)
                )
                .exists();
    }

    // 사용자의 게시글 조회 여부 확인
    private BooleanExpression viewedByMeExpression(UUID userId) {
        QArticleView viewedArticleView = new QArticleView("viewedArticleView");

        return JPAExpressions
                .selectOne()
                .from(viewedArticleView)
                .where(
                        viewedArticleView.article.eq(article),
                        viewedArticleView.user.id.eq(userId)
                )
                .exists();
    }

    // 키워드 검색 - 제목 OR 요약
    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        return article.title.containsIgnoreCase(keyword)
                .or(article.summary.containsIgnoreCase(keyword));
    }

    // 출처 필터
    private BooleanExpression sourceIn(List<ArticleSource> sources) {
        return sources != null && !sources.isEmpty()
                ? article.source.in(sources)
                : null;
    }

    // 시작 날짜 이상
    private BooleanExpression publishedAtGoe(Instant startDate) {
        return startDate != null
                ? article.publishDate.goe(startDate)
                : null;
    }

    // 종료 날짜 이하
    private BooleanExpression publishedAtLoe(Instant endDate) {
        return endDate != null
                ? article.publishDate.loe(endDate)
                : null;
    }

    // 댓글 수 / 조회 수 커서 페이지네이션
    private BooleanExpression cursorCondition(
            ArticleSearchCommand command,
            String orderBy
    ) {
        if (!StringUtils.hasText(command.cursor())
                || command.after() == null
                || command.afterId() == null) {
            return null;
        }

        if ("publishDate".equals(orderBy)) {
            return null;
        }

        Long cursorCount = Long.parseLong(command.cursor());

        boolean isDesc = "desc".equalsIgnoreCase(command.direction());

        NumberExpression<Long> countExpression;

        if ("commentCount".equals(orderBy)) {
            countExpression = commentCountExpression();
        } else if ("viewCount".equals(orderBy)) {
            countExpression = viewCountExpression();
        } else {
            return null;
        }

        if (isDesc) {
            return countExpression.lt(cursorCount)
                    .or(
                            countExpression.eq(cursorCount)
                                    .and(article.createdAt.lt(command.after()))
                    )
                    .or(
                            countExpression.eq(cursorCount)
                                    .and(article.createdAt.eq(command.after()))
                                    .and(article.id.lt(command.afterId()))
                    );
        }

        return countExpression.gt(cursorCount)
                .or(
                        countExpression.eq(cursorCount)
                                .and(article.createdAt.gt(command.after()))
                )
                .or(
                        countExpression.eq(cursorCount)
                                .and(article.createdAt.eq(command.after()))
                                .and(article.id.gt(command.afterId()))
                );
    }

    // 날짜 정렬 커서 페이지네이션
    private BooleanExpression publishDateCursorCondition(
            String orderBy,
            String sortDirection,
            String nextCursor,
            Instant nextAfter,
            UUID nextAfterId
    ) {
        if (!"publishDate".equals(orderBy)
                || !StringUtils.hasText(nextCursor)
                || nextAfter == null
                || nextAfterId == null) {
            return null;
        }

        Instant cursorDate = Instant.parse(nextCursor);

        boolean isDesc = "desc".equalsIgnoreCase(sortDirection);

        if (isDesc) {
            return article.publishDate.lt(cursorDate)
                    .or(
                            article.publishDate.eq(cursorDate)
                                    .and(article.createdAt.lt(nextAfter))
                    )
                    .or(
                            article.publishDate.eq(cursorDate)
                                    .and(article.createdAt.eq(nextAfter))
                                    .and(article.id.lt(nextAfterId))
                    );
        }

        return article.publishDate.gt(cursorDate)
                .or(
                        article.publishDate.eq(cursorDate)
                                .and(article.createdAt.gt(nextAfter))
                )
                .or(
                        article.publishDate.eq(cursorDate)
                                .and(article.createdAt.eq(nextAfter))
                                .and(article.id.gt(nextAfterId))
                );
    }

    // 동적 정렬 메서드
    private OrderSpecifier<?>[] orderSpecifiers(
            String orderBy,
            String sortDirection
    ) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        Order direction = "desc".equalsIgnoreCase(sortDirection)
                ? Order.DESC
                : Order.ASC;

        switch (orderBy) {
            case "commentCount":
                orderSpecifiers.add(
                        new OrderSpecifier<>(direction, commentCountExpression())
                );
                break;

            case "viewCount":
                orderSpecifiers.add(
                        new OrderSpecifier<>(direction, viewCountExpression())
                );
                break;

            default:
                orderSpecifiers.add(
                        new OrderSpecifier<>(direction, article.publishDate)
                );
        }

        // 동일한 정렬값일 경우 createdAt으로 순서 결정
        orderSpecifiers.add(new OrderSpecifier<>(direction, article.createdAt));

        // createdAt까지 동일한 경우 id로 순서 결정
        orderSpecifiers.add(new OrderSpecifier<>(direction, article.id));

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }

}
