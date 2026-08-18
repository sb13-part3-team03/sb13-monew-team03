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

  }
}
