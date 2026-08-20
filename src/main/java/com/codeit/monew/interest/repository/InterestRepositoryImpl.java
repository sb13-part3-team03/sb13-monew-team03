package com.codeit.monew.interest.repository;

import com.codeit.monew.interest.entity.QSubscription;
import com.codeit.monew.interest.repository.projection.InterestSearchResult;
import com.codeit.monew.interest.service.condition.InterestSearchCondition;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.codeit.monew.interest.entity.QInterest.interest;
import static com.codeit.monew.interest.entity.QSubscription.subscription;

@Repository
@RequiredArgsConstructor
public class InterestRepositoryImpl implements InterestRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private static final QSubscription userSubscription = new QSubscription("userSubscription");

    @Override
    public List<InterestSearchResult> search(InterestSearchCondition condition, int limit) {
        return queryFactory
                .select(
                        Projections.constructor(
                                InterestSearchResult.class,
                                interest,
                                subscriberCount(),
                                subscribedByMe()
                        )
                )
                .from(interest)

                // 전체 구독자 수
                .leftJoin(subscription)
                .on(subscription.interest.eq(interest))

                // 요청자의 구독 여부
                .leftJoin(userSubscription)
                .on(
                        userSubscription.interest.eq(interest)
                                .and(
                                        userSubscription.user.id
                                                .eq(condition.userId())
                                )
                )

                .where(
                        keywordContains(condition.keyword()),
                        nameCursorCondition(condition)
                )
                .groupBy(interest)
                .having(
                        subscriberCountCursorCondition(condition)
                )
                .orderBy(
                        orderSpecifiers(condition)
                )
                .limit(limit)
                .fetch();
    }

    @Override
    public long countByCondition(InterestSearchCondition condition) {
        Long count = queryFactory
                .select(interest.id.countDistinct())
                .from(interest)
                .where(
                        keywordContains(condition.keyword())
                )
                .fetchOne();

        return count != null ? count : 0L;
    }

    //keyword 검색
    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return interest.name.containsIgnoreCase(keyword)
                .or(
                        interest.keywords.any()
                                .containsIgnoreCase(keyword)
                );
    }

    //구독자 수 가져오기
    private NumberExpression<Long> subscriberCount() {
        return subscription.id.count();
    }

    //내 구독 여부 확인
    private BooleanExpression subscribedByMe() {
        return userSubscription.id.count().gt(0L);
    }

    // 정렬 속성 기준 ASC/DESC 정렬, 보조 커서(createdAt) 값 정렬 포함
    private OrderSpecifier<?>[] orderSpecifiers(InterestSearchCondition condition) {
        boolean desc = "DESC".equalsIgnoreCase(condition.direction());

        OrderSpecifier<?> primaryOrder;

        if ("subscriberCount".equals(condition.orderBy())) {
            primaryOrder = desc
                    ? subscriberCount().desc()
                    : subscriberCount().asc();
        } else {
            primaryOrder = desc
                    ? interest.name.desc()
                    : interest.name.asc();
        }

        OrderSpecifier<?> createdAtOrder = desc
                ? interest.createdAt.desc()
                : interest.createdAt.asc();

        return new OrderSpecifier<?>[]{
                primaryOrder,
                createdAtOrder
        };
    }

    // 이름 정렬 시 현재 커서 이후 데이터를 조회하기 위한 조건
    private BooleanExpression nameCursorCondition(
            InterestSearchCondition condition
    ) {
        if (!"name".equals(condition.orderBy())
                || condition.cursor() == null
                || condition.cursor().isBlank()
                || condition.after() == null) {
            return null;
        }

        boolean desc =
                "DESC".equalsIgnoreCase(condition.direction());

        String cursor = condition.cursor();

        BooleanExpression primaryCondition = desc
                ? interest.name.lt(cursor)
                : interest.name.gt(cursor);

        BooleanExpression sameValueCondition =
                interest.name.eq(cursor)
                        .and(
                                desc
                                        ? interest.createdAt.lt(condition.after())
                                        : interest.createdAt.gt(condition.after())
                        );

        return primaryCondition.or(sameValueCondition);
    }

    // 구독자 수 정렬 시 현재 커서 이후 데이터를 조회하기 위한 집계 조건
    private BooleanExpression subscriberCountCursorCondition(
            InterestSearchCondition condition
    ) {
        if (!"subscriberCount".equals(condition.orderBy())
                || condition.cursor() == null
                || condition.cursor().isBlank()
                || condition.after() == null) {
            return null;
        }

        boolean desc =
                "DESC".equalsIgnoreCase(condition.direction());

        long cursor = Long.parseLong(condition.cursor());

        BooleanExpression primaryCondition = desc
                ? subscriberCount().lt(cursor)
                : subscriberCount().gt(cursor);

        BooleanExpression sameValueCondition =
                subscriberCount().eq(cursor)
                        .and(
                                desc
                                        ? interest.createdAt.lt(condition.after())
                                        : interest.createdAt.gt(condition.after())
                        );

        return primaryCondition.or(sameValueCondition);
    }
}
