package com.codeit.monew.notification.dto.response;

import com.codeit.monew.notification.entity.Notification;
import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    Instant createdAt,
    Instant updatedAt,
    boolean confirmed,
    UUID userId,
    String content,
    String resourceType,
    UUID resourceId
) {

  public static NotificationDto from(Notification notification) {
    return new NotificationDto(
        notification.getId(),
        notification.getCreatedAt(),
        notification.getUpdatedAt(),
        notification.isConfirmed(),
        notification.getUserId(),
        notification.getContent(),
        notification.getResourceType(),
        notification.getResourceId()
    );
  }
}
