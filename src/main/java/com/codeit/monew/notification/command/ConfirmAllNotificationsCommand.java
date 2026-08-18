package com.codeit.monew.notification.command;

import java.util.UUID;

public record ConfirmAllNotificationsCommand(
    UUID userId
) {

  public ConfirmAllNotificationsCommand {

  }
}
