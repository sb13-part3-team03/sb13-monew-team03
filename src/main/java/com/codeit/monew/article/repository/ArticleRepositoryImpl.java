package com.codeit.monew.article.repository;

import static com.codeit.monew.article.entity.QArticle.article;
import static com.codeit.monew.article.entity. QArticleInterest.articleInterest;
import static com.codeit.monew.article.entity.QArticleView.articleView;
import static com.codeit.monew.comment.entity.QComment.comment;

import com.codeit.monew.article.dto.request.ArticleSearchRequest;
import com.codeit.monew.article.dto.response.ArticleSearchResult;

import com.codeit.monew.article.entity.ArticleSource;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public long countTotalElements(ArticleSearchRequest request) {

        Long count = queryFactory
                .select(article.id.countDistinct())
                .from(article)
                .where(
                        keywordContains(request.keyword()),
                        interestIdEq(request.interestId()),
                        sourceEq(request.sourceIn()),
                        publishedAtGoe(request.publishDateFrom()),
                        publishedAtLoe(request.publishDateTo())
                )
                .fetchOne();

        return count != null ? count : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleSearchResult> searchArticles(ArticleSearchRequest request) {

        return queryFactory
                .select(Projections.constructor(
                        ArticleSearchResult.class,
                        article,
                        comment.id.countDistinct(),
                        articleView.id.countDistinct()
                ))
                .from(article)
                .leftJoin(articleInterest)
                .on(articleInterest.article.eq(article))
                .leftJoin(comment)
                .on(comment.article.eq(article))
                .leftJoin(articleView)
                .on(articleView.article.eq(article))
                .where(
                        keywordContains(request.keyword()),
                        interestIdEq(request.interestId()),
                        sourceEq(request.sourceIn()),
                        publishedAtGoe(request.publishDateFrom()),
                        publishedAtLoe(request.publishDateTo()),
                        publishedDateCursorCondition(
                                request.direction(),
                                request.cursor(),
                                request.after()
                        )
                )
                .groupBy(article.id)
                .having(
                        // comment 및 interest는 집계 함수로 having 절에서 정렬
                        cursorHavingCondition(request)
                )
                .orderBy(
                        orderSpecifiers(
                        request.orderBy(),
                        request.direction()
                    )
                )
                .limit(request.limit() + 1)
                .fetch();

    }

    // 댓글 수 / 조회 수 정렬 커서 페이지네이션
    private BooleanExpression cursorHavingCondition(ArticleSearchRequest request) {
        if (!StringUtils.hasText(request.cursor())
                || request.after() == null) {
            return null;
        }

        Long cursorCount = Long.parseLong(request.cursor());

        boolean isDesc =
                "desc".equalsIgnoreCase(request.direction());

        NumberExpression<Long> countExpression;

        if ("commentCount".equals(request.orderBy())) {
            countExpression = comment.id.countDistinct();
        } else if ("viewCount".equals(request.orderBy())) {
            countExpression = articleView.id.countDistinct();
        } else {
            return null;
        }

        if (isDesc) {
            return countExpression.lt(cursorCount)
                    .or(
                            countExpression.eq(cursorCount)
                                    .and(article.id.lt(request.after()))
                    );
        }

        return countExpression.gt(cursorCount)
                .or(
                        countExpression.eq(cursorCount)
                                .and(article.id.gt(request.after()))
                );
    }

    // 날짜 정렬 커서 페이지네이션
    private BooleanExpression publishedDateCursorCondition(
            String sortDirection,
            String nextCursor,
            UUID nextAfter
    ) {
        if (!StringUtils.hasText(nextCursor) || nextAfter == null) {
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
            String sortField,
            String sortDirection
    ) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        if (!StringUtils.hasText(sortField)) {
            sortField = "publishDate";
        }

        Order direction = "desc".equalsIgnoreCase(sortDirection)
                ? Order.DESC
                : Order.ASC;

        switch (sortField) {
            case "commentCount":
                orderSpecifiers.add(
                        new OrderSpecifier<>(
                                direction,
                                comment.id.countDistinct()
                        )
                );
                break;

            case "viewCount":
                orderSpecifiers.add(
                        new OrderSpecifier<>(
                                direction,
                                articleView.id.countDistinct()
                        )
                );
                break;

            default:
                orderSpecifiers.add(
                        new OrderSpecifier<>(
                                direction,
                                article.publishDate
                        )
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

    // 관심사 필터
    private BooleanExpression interestIdEq(UUID interestId) {
        return interestId != null
                ? articleInterest.interest.id.eq(interestId)
                : null;
    }

    // 출처 필터
    private BooleanExpression sourceEq(ArticleSource source) {
        if (source == null) {
            return null;
        }

        return article.source.eq(source);
    }

    // 시작 날짜 이상
    private BooleanExpression publishedAtGoe(LocalDateTime startDate) {
        return startDate != null
                ? article.publishDate.goe(Instant.from(startDate.atZone(ZoneId.of("Asia/Seoul")).toInstant()))
                : null;
    }

    // 종료 날짜 이하
    private BooleanExpression publishedAtLoe(LocalDateTime endDate) {
        return endDate != null
                ? article.publishDate.loe(Instant.from(endDate.atZone(ZoneId.of("Asia/Seoul")).toInstant()))
                : null;
    }

}
