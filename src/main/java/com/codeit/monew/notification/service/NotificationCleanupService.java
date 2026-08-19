package com.codeit.monew.notification.service;

import com.codeit.monew.notification.repository.NotificationRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCleanupService {

  private static final long RETENTION_DAYS = 7;

  private final NotificationRepository notificationRepository;

  @Transactional
  public void deleteOldConfirmedNotifications() {
    Instant threshold = Instant.now().minus(RETENTION_DAYS, ChronoUnit.DAYS);
    int deletedCount = notificationRepository.deleteConfirmedBefore(threshold);

    log.info("확인된 알림 삭제 완료: count={}", deletedCount);
  }
}
