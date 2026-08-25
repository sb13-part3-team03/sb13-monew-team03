package com.codeit.monew.notification.event;

import com.codeit.monew.notification.enums.ResourceType;
import java.util.UUID;

public record NotificationCreateEvent(
        String content,
        UUID userId,
        ResourceType resourceType,
        UUID resourceId
) {
}
