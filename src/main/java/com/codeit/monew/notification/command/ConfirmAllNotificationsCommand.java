package com.codeit.monew.notification.command;

import java.util.UUID;

public record ConfirmAllNotificationsCommand(
    UUID userId
) {

  public ConfirmAllNotificationsCommand {
    if (userId == null) {
      throw new IllegalArgumentException("사용자 ID는 필수입니다.");
    }
  }
}
