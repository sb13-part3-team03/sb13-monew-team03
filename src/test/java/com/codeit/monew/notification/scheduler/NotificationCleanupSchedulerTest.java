package com.codeit.monew.notification.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

import com.codeit.monew.notification.service.NotificationCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

@ExtendWith(MockitoExtension.class)
@DisplayName("알림 정리 스케줄러 테스트")
class NotificationCleanupSchedulerTest {

  @Mock
  private NotificationCleanupService cleanupService;

  @InjectMocks
  private NotificationCleanupScheduler scheduler;

  @Test
  @DisplayName("매일 서울 시간 자정에 오래된 알림을 정리")
  void cleanUp() throws NoSuchMethodException {
    Scheduled schedule = NotificationCleanupScheduler.class.getMethod("cleanUp")
        .getAnnotation(Scheduled.class);

    assertThat(schedule).isNotNull();
    assertThat(schedule.cron()).isEqualTo("0 0 0 * * *");
    assertThat(schedule.zone()).isEqualTo("Asia/Seoul");

    scheduler.cleanUp();

    then(cleanupService).should().deleteOldConfirmedNotifications();
  }
}
