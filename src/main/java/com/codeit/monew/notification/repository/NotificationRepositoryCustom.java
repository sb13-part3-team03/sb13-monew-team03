package com.codeit.monew.notification.repository;

import com.codeit.monew.notification.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationRepositoryCustom {

  List<Notification> findAllNotConfirmed(
      UUID userId,
      Instant after,
      UUID cursor,
      int limit
  );
}
