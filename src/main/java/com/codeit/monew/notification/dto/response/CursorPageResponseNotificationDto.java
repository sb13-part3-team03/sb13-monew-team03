package com.codeit.monew.notification.dto.response;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseNotificationDto(
    List<NotificationDto> content,
    String nextCursor,
    Instant nextAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

  public CursorPageResponseNotificationDto {
    content = List.copyOf(content);
  }
}
