package com.codeit.monew.notification.scheduler;

import com.codeit.monew.notification.service.NotificationCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

  private final NotificationCleanupService cleanupService;

  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
  public void cleanUp() {
    cleanupService.deleteOldConfirmedNotifications();
  }
}
