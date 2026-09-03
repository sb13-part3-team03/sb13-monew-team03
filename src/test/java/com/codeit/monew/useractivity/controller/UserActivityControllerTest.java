package com.codeit.monew.useractivity.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.monew.global.exception.GlobalExceptionHandler;
import com.codeit.monew.useractivity.dto.response.UserActivityDto;
import com.codeit.monew.useractivity.exception.UserActivityNotFoundException;
import com.codeit.monew.useractivity.service.UserActivityService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("활동내역 컨트롤러 테스트")
class UserActivityControllerTest {

  @Mock
  private UserActivityService userActivityService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new UserActivityController(userActivityService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  @DisplayName("활동내역 조회에 성공하면 200 응답을 반환")
  void find_returnsUserActivity() throws Exception {
    UUID userId = UUID.randomUUID();
    UserActivityDto activity = new UserActivityDto(
        userId, "user@example.com", "nickname", Instant.now(),
        List.of(), List.of(), List.of(), List.of());
    given(userActivityService.find(userId)).willReturn(activity);

    mockMvc.perform(get("/api/user-activities/{userId}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()));
  }

  @Test
  @DisplayName("활동내역이 없으면 404 응답을 반환")
  void find_whenActivityDoesNotExist_returnsNotFound() throws Exception {
    UUID userId = UUID.randomUUID();
    given(userActivityService.find(userId)).willThrow(new UserActivityNotFoundException());

    mockMvc.perform(get("/api/user-activities/{userId}", userId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USER_ACTIVITY_NOT_FOUND"));
  }
}
