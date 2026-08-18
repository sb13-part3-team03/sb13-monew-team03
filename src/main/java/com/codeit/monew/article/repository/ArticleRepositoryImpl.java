package com.codeit.monew.article.repository;

import static com.codeit.monew.article.entity.QArticle.article;
import static com.codeit.monew.article.entity.QArticleView.articleView;
import static com.codeit.monew.article.entity. QArticleInterest.articleInterest;
import static com.codeit.monew.comment.entity.QComment.comment;

import com.codeit.monew.article.dto.request.ArticleSearchRequest;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleSource;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
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
    public List<Article> searchArticles(ArticleSearchRequest request) {

        return queryFactory
                .selectFrom(article)
                .leftJoin(articleInterest)
                .on(articleInterest.article.eq(article))
                .where(
                        keywordContains(request.keyword()),
                        interestIdEq(request.interestId()),
                        sourceEq(request.sourceIn()),
                        publishedAtGoe(request.publishDateFrom()),
                        publishedAtLoe(request.publishDateTo())
                )
                .orderBy(orderSpecifiers(
                        request.orderBy(),
                        request.direction()
                ))
                .limit(request.limit() + 1)
                .fetch();

    }

    // 동적 정렬 메서드
    private OrderSpecifier<?>[] orderSpecifiers(String sortField, String sortDirection) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        if (!StringUtils.hasText(sortField)) {
            sortField = "publishDate";
        }

        Order direction = "desc".equalsIgnoreCase(sortDirection)
                ? Order.DESC
                : Order.ASC;

        switch (sortField) {
            case "commentCount":
                orderSpecifiers.add(new OrderSpecifier<>(direction, comment.count()));
                break;
            case "viewCount":
                orderSpecifiers.add(new OrderSpecifier<>(direction, articleView.count()));
                break;
            default:
                orderSpecifiers.add(new OrderSpecifier<>(direction, article.publishDate));
        }

        // 커서 페이지네이션 안정성을 위한 동일 방향 정렬
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
