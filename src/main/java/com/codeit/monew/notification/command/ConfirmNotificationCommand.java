package com.codeit.monew.notification.command;

import java.util.UUID;

public record ConfirmNotificationCommand(
    UUID notificationId,
    UUID userId
) {

  public ConfirmNotificationCommand {
    if (notificationId == null) {
      throw new IllegalArgumentException("알림 ID는 필수입니다.");
    }
    if (userId == null) {
      throw new IllegalArgumentException("사용자 ID는 필수입니다.");
    }
  }
}
