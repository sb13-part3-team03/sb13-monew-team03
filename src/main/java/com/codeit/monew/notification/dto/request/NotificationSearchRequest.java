package com.codeit.monew.notification.dto.request;

import com.codeit.monew.notification.condition.NotificationSearchCondition;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record NotificationSearchRequest(
    String cursor,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
    @Positive int limit
) {

  public NotificationSearchCondition toCondition(UUID userId) {
    return new NotificationSearchCondition(userId, cursor, after, limit);
  }
}
