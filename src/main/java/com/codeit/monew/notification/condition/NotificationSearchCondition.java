package com.codeit.monew.notification.condition;

import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.global.exception.MonewException;
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
      throw new MonewException(ErrorCode.INVALID_INPUT_VALUE);
    }
  }
}
