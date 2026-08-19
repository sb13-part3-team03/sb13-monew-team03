package com.codeit.monew.notification.dto.response;

import com.codeit.monew.notification.entity.Notification;
import com.codeit.monew.notification.enums.ResourceType;
import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    Instant createdAt,
    Instant updatedAt,
    boolean confirmed,
    UUID userId,
    String content,
    ResourceType resourceType,
    UUID resourceId
) {

  public static NotificationDto from(Notification notification) {
    return new NotificationDto(
        notification.getId(),
        notification.getCreatedAt(),
        notification.getUpdatedAt(),
        notification.getConfirmed(),
        notification.getUserId(),
        notification.getContent(),
        notification.getResourceType(),
        notification.getResourceId()
    );
  }
}
