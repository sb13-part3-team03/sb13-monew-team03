package com.codeit.monew.notification.repository;

import com.codeit.monew.notification.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository
    extends JpaRepository<Notification, UUID>, NotificationRepositoryCustom {

  //confirm되지 않은 항목 반환
  List<Notification> findByUserIdAndConfirmedFalse(UUID userId);

  Optional<Notification> findByIdAndUserId(UUID notificationId, UUID userId);

  long countByUserIdAndConfirmedFalse(UUID userId);

  void deleteAllByUserId(UUID userId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      delete from Notification n
      where n.confirmed = true
        and n.updatedAt < :threshold
      """)
  int deleteConfirmedBefore(@Param("threshold") Instant threshold);
}
