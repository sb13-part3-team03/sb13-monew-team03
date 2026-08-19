package com.codeit.monew.notification.repository;

import static com.codeit.monew.notification.entity.QNotification.notification;

import com.codeit.monew.notification.entity.Notification;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Notification> findAllNotConfirmed(
      UUID userId,
      Instant after,
      UUID cursor,
      int limit
  ) {
    return queryFactory
        .selectFrom(notification)
        .where(
            notification.userId.eq(userId),
            notification.confirmed.isFalse(),
            cursorCondition(after, cursor)
        )
        .orderBy(
            notification.createdAt.desc(),
            notification.id.desc()
        )
        .limit(limit)
        .fetch();
  }

  private BooleanExpression cursorCondition(Instant after, UUID cursor) {
    if (after == null || cursor == null) {
      return null;
    }

    return notification.createdAt.lt(after)
        .or(notification.createdAt.eq(after).and(notification.id.lt(cursor)));
  }
}
