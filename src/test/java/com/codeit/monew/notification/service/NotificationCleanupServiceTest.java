package com.codeit.monew.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.codeit.monew.notification.repository.NotificationRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("오래된 알림 정리 서비스 테스트")
class NotificationCleanupServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @InjectMocks
  private NotificationCleanupService cleanupService;

  @Test
  @DisplayName("7일보다 오래된 확인 알림을 삭제")
  void deleteOldConfirmedNotifications() {
    ArgumentCaptor<Instant> thresholdCaptor = ArgumentCaptor.forClass(Instant.class);
    given(notificationRepository.deleteConfirmedBefore(thresholdCaptor.capture())).willReturn(2);
    Instant earliestThreshold = Instant.now().minus(7, ChronoUnit.DAYS);

    cleanupService.deleteOldConfirmedNotifications();

    Instant latestThreshold = Instant.now().minus(7, ChronoUnit.DAYS);
    then(notificationRepository).should().deleteConfirmedBefore(thresholdCaptor.getValue());
    assertThat(thresholdCaptor.getValue()).isBetween(earliestThreshold, latestThreshold);
  }
}
