package com.codeit.monew.notification.command;

import java.util.UUID;

public record ConfirmNotificationCommand(
    UUID notificationId,
    UUID userId
) {

  public ConfirmNotificationCommand {

  }
}
