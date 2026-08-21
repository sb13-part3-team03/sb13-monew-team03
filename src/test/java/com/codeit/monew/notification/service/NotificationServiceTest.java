package com.codeit.monew.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.global.exception.MonewException;
import com.codeit.monew.notification.condition.NotificationSearchCondition;
import com.codeit.monew.notification.dto.response.CursorPageResponseNotificationDto;
import com.codeit.monew.notification.entity.Notification;
import com.codeit.monew.notification.enums.ResourceType;
import com.codeit.monew.notification.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  NotificationRepository repository;
  @InjectMocks
  NotificationService service;


  @Test
  @DisplayName("컨펌되지 않은 항목을 커서페이지네이션으로 조회")
  void findAllNotConfirmed() {
    // given
    UUID userId = UUID.randomUUID();
    UUID cursor = UUID.randomUUID();
    Instant after = Instant.parse("2026-08-19T00:00:00Z");
    NotificationSearchCondition condition =
        new NotificationSearchCondition(userId, cursor.toString(), after, 2);

    Notification first = notification(userId, "첫 번째 알림",
        Instant.parse("2026-08-18T03:00:00Z"));
    Notification second = notification(userId, "두 번째 알림",
        Instant.parse("2026-08-18T02:00:00Z"));
    Notification extra = mock(Notification.class);

    given(repository.findAllNotConfirmed(userId, after, cursor, 3))
        .willReturn(List.of(first, second, extra));
    given(repository.countByUserIdAndConfirmedFalse(userId)).willReturn(5L);

    // when
    CursorPageResponseNotificationDto result = service.findAllNotConfirmed(condition);

    // then
    assertThat(result.content()).hasSize(2);
    assertThat(result.content())
        .extracting("content")
        .containsExactly("첫 번째 알림", "두 번째 알림");
    assertThat(result.nextCursor()).isEqualTo(second.getId().toString());
    assertThat(result.nextAfter()).isEqualTo(second.getCreatedAt());
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.totalElements()).isEqualTo(5L);
    assertThat(result.hasNext()).isTrue();

    then(repository).should().findAllNotConfirmed(userId, after, cursor, 3);
    then(repository).should().countByUserIdAndConfirmedFalse(userId);
  }

  private Notification notification(UUID userId, String content, Instant createdAt) {
    Notification notification = mock(Notification.class);
    given(notification.getId()).willReturn(UUID.randomUUID());
    given(notification.getCreatedAt()).willReturn(createdAt);
    given(notification.getUpdatedAt()).willReturn(createdAt);
    given(notification.getConfirmed()).willReturn(false);
    given(notification.getUserId()).willReturn(userId);
    given(notification.getContent()).willReturn(content);
    given(notification.getResourceType()).willReturn(ResourceType.COMMENT);
    given(notification.getResourceId()).willReturn(UUID.randomUUID());
    return notification;
  }



  @Test
  @DisplayName("알림 생성")
  void create() {
    // given
    String content = "알림";
    UUID userId = UUID.randomUUID();
    UUID resourceId = UUID.randomUUID();

    // when
    service.create(content, userId, ResourceType.COMMENT, resourceId);

    // then
    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    then(repository).should().save(captor.capture());

    Notification savedNotification = captor.getValue();
    assertThat(savedNotification.getContent()).isEqualTo(content);
    assertThat(savedNotification.getUserId()).isEqualTo(userId);
    assertThat(savedNotification.getResourceType()).isEqualTo(ResourceType.COMMENT);
    assertThat(savedNotification.getResourceId()).isEqualTo(resourceId);
    assertThat(savedNotification.getConfirmed()).isFalse();
  }

  @Test
  @DisplayName("알림 모두 확인")
  void confirmAll() {
    // given
    UUID userId = UUID.randomUUID();
    Notification first = mock(Notification.class);
    Notification second = mock(Notification.class);
    List<Notification> notifications = List.of(first, second);
    given(repository.findByUserIdAndConfirmedFalse(userId)).willReturn(notifications);

    // when
    service.confirmAll(userId);

    // then
    then(repository).should().findByUserIdAndConfirmedFalse(userId);
    then(first).should().confirm();
    then(second).should().confirm();
    then(repository).should().saveAll(notifications);
  }

  @Test
  @DisplayName("알림 확인")
  void confirm() {
    UUID notificationId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Notification notification = mock(Notification.class);
    given(repository.findByIdAndUserId(notificationId, userId))
        .willReturn(Optional.of(notification));

    service.confirm(notificationId, userId);

    then(notification).should().confirm();
    then(repository).should().save(notification);
  }


}
