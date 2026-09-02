package com.codeit.monew.notification.listener;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;

import com.codeit.monew.notification.enums.ResourceType;
import com.codeit.monew.notification.event.NotificationCreateEvent;
import com.codeit.monew.notification.service.NotificationService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("알림 생성 이벤트 리스너 테스트")
class NotificationCreateEventListenerTest {

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private NotificationCreateEventListener listener;

  @Test
  @DisplayName("커밋된 이벤트의 정보로 새 트랜잭션에서 알림 생성")
  void handle() {
    UUID userId = UUID.randomUUID();
    UUID resourceId = UUID.randomUUID();
    NotificationCreateEvent event =
        new NotificationCreateEvent("알림", userId, ResourceType.COMMENT, resourceId);

    listener.handle(event);

    then(notificationService).should()
        .createInNewTransaction("알림", userId, ResourceType.COMMENT, resourceId);
  }

  @Test
  @DisplayName("알림 생성 실패가 이미 커밋된 원본 작업에 전파되지 않음")
  void handle_whenCreationFails_doesNotPropagateException() {
    UUID userId = UUID.randomUUID();
    UUID resourceId = UUID.randomUUID();
    NotificationCreateEvent event =
        new NotificationCreateEvent("알림", userId, ResourceType.COMMENT, resourceId);
    doThrow(new RuntimeException("creation failed"))
        .when(notificationService)
        .createInNewTransaction("알림", userId, ResourceType.COMMENT, resourceId);

    assertThatCode(() -> listener.handle(event)).doesNotThrowAnyException();
  }
}
