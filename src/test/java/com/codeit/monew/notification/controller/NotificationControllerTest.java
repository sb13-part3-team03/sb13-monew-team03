package com.codeit.monew.notification.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.monew.notification.condition.NotificationSearchCondition;
import com.codeit.monew.notification.dto.response.CursorPageResponseNotificationDto;
import com.codeit.monew.notification.dto.response.NotificationDto;
import com.codeit.monew.notification.enums.ResourceType;
import com.codeit.monew.notification.service.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
@DisplayName("ActivityController 웹 계층 테스트")
class NotificationControllerTest {

  @Autowired
  MockMvc mvc;

  @MockitoBean
  NotificationService service;

  UUID userId;
  NotificationSearchCondition condition;
  CursorPageResponseNotificationDto response;
  List<NotificationDto> notifications;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    condition = new NotificationSearchCondition(userId, null, null, 20);

    notifications = List.of(
        new NotificationDto(UUID.randomUUID(), Instant.now(), Instant.now(), false, userId,
            "댓글1", ResourceType.COMMENT, UUID.randomUUID()),
        new NotificationDto(UUID.randomUUID(), Instant.now(), Instant.now(), true, userId,
            "댓글2", ResourceType.COMMENT, UUID.randomUUID()),
        new NotificationDto(UUID.randomUUID(), Instant.now(), Instant.now(), false, userId,
            "댓글3", ResourceType.COMMENT, UUID.randomUUID()),
        new NotificationDto(UUID.randomUUID(), Instant.now(), Instant.now(), true, userId,
            "관심사1", ResourceType.INTEREST, UUID.randomUUID()),
        new NotificationDto(UUID.randomUUID(), Instant.now(), Instant.now(), false, userId,
            "관심사2", ResourceType.INTEREST, UUID.randomUUID())
    );

    List<NotificationDto> confirmedNotifications = notifications.stream()
        .filter(NotificationDto::confirmed)
        .toList();
    response = new CursorPageResponseNotificationDto(
        confirmedNotifications, null, null, confirmedNotifications.size(),
        confirmedNotifications.size(), false);
  }
  

  @Test
  @DisplayName("컨펌되지않은 항목 반환")
  void findAllNotConfirmed() throws Exception {

    // given
    given(service.findAllNotConfirmed(condition)).willReturn(response);

    // when & then
    mvc.perform(get("/api/notifications")
            .header("Monew-Request-User-ID", userId)
            .param("limit", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].confirmed").value(true))
        .andExpect(jsonPath("$.content[1].confirmed").value(true))
        .andExpect(jsonPath("$.content[0].content").value("댓글2"))
        .andExpect(jsonPath("$.content[1].content").value("구독1"))
        .andExpect(jsonPath("$.size").value(2))
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.hasNext").value(false));

    verify(service).findAllNotConfirmed(condition);
  }


  @Test
  @DisplayName("알림 모두 확인")
  void confirmAllStatus400() throws Exception {
    // when & then
    mvc.perform(patch("/api/notifications")
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isOk());

    verify(service).confirmAll(userId);
  }

  @Test
  @DisplayName("알림 단건 확인")
  void confirmStatus400() throws Exception {
    // given
    UUID notificationId = UUID.randomUUID();

    // when & then
    mvc.perform(patch("/api/notifications/{notificationId}", notificationId)
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isOk());

    verify(service).confirm(notificationId, userId);
  }
}
