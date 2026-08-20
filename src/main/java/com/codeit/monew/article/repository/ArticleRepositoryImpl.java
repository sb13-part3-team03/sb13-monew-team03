package com.codeit.monew.article.repository;

import static com.codeit.monew.article.entity.QArticle.article;
import static com.codeit.monew.article.entity. QArticleInterest.articleInterest;
import static com.codeit.monew.article.entity.QArticleView.articleView;
import static com.codeit.monew.comment.entity.QComment.comment;

import com.codeit.monew.article.dto.command.ArticleSearchCommand;
import com.codeit.monew.article.dto.response.ArticleSearchResult;

import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.article.entity.QArticleInterest;
import com.codeit.monew.article.entity.QArticleView;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
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
    public List<ArticleSearchResult> searchArticles(ArticleSearchCommand command, String orderBy) {

        return queryFactory
                .select(Projections.constructor(
                        ArticleSearchResult.class,
                        article,
                        comment.id.countDistinct(),
                        articleView.id.countDistinct(),
                        // 현재 user가 이 article을 조회했는지
                        viewedByMeExpression(command.userId())
                ))
                .from(article)
                .leftJoin(comment)
                .on(comment.article.eq(article))
                .leftJoin(articleView)
                .on(articleView.article.eq(article))
                .where(
                        keywordContains(command.keyword()),
                        hasInterestExpression(command.interestId()),
                        sourceIn(command.sourceIn()),
                        publishedAtGoe(command.publishDateFrom()),
                        publishedAtLoe(command.publishDateTo()),
                        publishedDateCursorCondition(
                                orderBy,
                                command.direction(),
                                command.cursor(),
                                command.after()
                        )
                )
                .groupBy(article.id)
                .having(
                        // comment 및 interest는 집계 함수로 having 절에서 정렬
                        cursorHavingCondition(command, orderBy)
                )
                .orderBy(
                        orderSpecifiers(orderBy, command.direction())
                )
                .limit(command.limit() + 1)
                .fetch();

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

    // 댓글 수 / 조회 수 정렬 커서 페이지네이션
    private BooleanExpression cursorHavingCondition(ArticleSearchCommand command, String orderBy) {
        if (!StringUtils.hasText(command.cursor())
                || command.after() == null) {
            return null;
        }

        // publishDate는 WHERE의 publishedDateCursorCondition에서 처리
        if ("publishDate".equals(orderBy)) {
            return null;
        }

        Long cursorCount = Long.parseLong(command.cursor());

        boolean isDesc = "desc".equalsIgnoreCase(command.direction());

        NumberExpression<Long> countExpression;

        if ("commentCount".equals(orderBy)) {
            countExpression = comment.id.countDistinct();
        } else if ("viewCount".equals(orderBy)) {
            countExpression = articleView.id.countDistinct();
        } else {
            return null;
        }

        if (isDesc) {
            return countExpression.lt(cursorCount)
                    .or(
                            countExpression.eq(cursorCount)
                                    .and(article.id.lt(command.after()))
                    );
        }

        return countExpression.gt(cursorCount)
                .or(
                        countExpression.eq(cursorCount)
                                .and(article.id.gt(command.after()))
                );
    }

    // 날짜 정렬 커서 페이지네이션
    private BooleanExpression publishedDateCursorCondition(
            String orderBy,
            String sortDirection,
            String nextCursor,
            UUID nextAfter
    ) {
        if (!"publishDate".equals(orderBy)
                || !StringUtils.hasText(nextCursor)
                || nextAfter == null) {
            return null;
        }

        Instant cursorDate = Instant.parse(nextCursor);
        boolean isDesc = "desc".equalsIgnoreCase(sortDirection);

        if (isDesc) {
            return article.publishDate.lt(cursorDate)
                    .or(
                            article.publishDate.eq(cursorDate)
                                    .and(article.id.lt(nextAfter))
                    );
        }

        return article.publishDate.gt(cursorDate)
                .or(
                        article.publishDate.eq(cursorDate)
                                .and(article.id.gt(nextAfter))
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
                        new OrderSpecifier<>(direction, comment.id.countDistinct())
                );
                break;

            case "viewCount":
                orderSpecifiers.add(
                        new OrderSpecifier<>(direction, articleView.id.countDistinct())
                );
                break;

            default:
                orderSpecifiers.add(
                        new OrderSpecifier<>(direction, article.publishDate)
                );
        }

        // 동일한 정렬값일 경우 UUID로 순서 결정
        orderSpecifiers.add(new OrderSpecifier<>(direction, article.id));

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
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

}
