package com.codeit.monew.useractivity.listener;

import static org.mockito.Mockito.verify;

import com.codeit.monew.useractivity.event.UserActivityEvent;
import com.codeit.monew.useractivity.service.UserActivityProjectionService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("활동내역 이벤트 리스너 테스트")
class UserActivityEventListenerTest {

  @Mock
  private UserActivityProjectionService projectionService;

  @InjectMocks
  private UserActivityEventListener listener;

  @Test
  @DisplayName("수신한 이벤트를 프로젝션 서비스에 전달")
  void handleDelegatesEvent() {
    UserActivityEvent event = new UserActivityEvent.UserRemoved(UUID.randomUUID());

    listener.handle(event);

    verify(projectionService).apply(event);
  }

}
