package com.codeit.monew.notification.repository;

import com.codeit.monew.notification.entity.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository
    extends JpaRepository<Notification, UUID>, NotificationRepositoryCustom {

  //confirm되지 않은 항목 반환
  List<Notification> findByUserIdAndConfirmedFalse(UUID userId);

  long countByUserIdAndConfirmedFalse(UUID userId);
}
