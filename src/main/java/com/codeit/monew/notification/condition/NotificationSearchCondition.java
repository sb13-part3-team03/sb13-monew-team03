package com.codeit.monew.notification.condition;

import java.time.Instant;
import java.util.UUID;

public record NotificationSearchCondition(
    UUID userId,
    String cursor,
    Instant after,
    int limit
) {

  public NotificationSearchCondition {
    boolean onlyCursorProvided = cursor != null && after == null;
    boolean onlyAfterProvided = cursor == null && after != null;
    if (onlyCursorProvided || onlyAfterProvided) {
      throw new IllegalArgumentException("cursor와 after는 함께 전달해야 합니다.");
    }
  }
}
