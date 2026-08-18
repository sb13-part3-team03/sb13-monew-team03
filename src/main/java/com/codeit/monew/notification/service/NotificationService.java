package com.codeit.monew.notification.service;

import com.codeit.monew.notification.dto.response.CursorPageResponseNotificationDto;
import com.codeit.monew.notification.command.ConfirmAllNotificationsCommand;
import com.codeit.monew.notification.command.ConfirmNotificationCommand;
import com.codeit.monew.notification.condition.NotificationSearchCondition;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

  public CursorPageResponseNotificationDto findAllNotConfirmed(
      NotificationSearchCondition condition
  ) {
    return null;
  }

  public void confirmAll(ConfirmAllNotificationsCommand command) {
  }

  public void confirm(ConfirmNotificationCommand command) {

  }

}
