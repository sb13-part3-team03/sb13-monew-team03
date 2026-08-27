package com.codeit.monew.useractivity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.codeit.monew.interest.repository.SubscriptionRepository;
import com.codeit.monew.useractivity.dto.response.UserActivityDto;
import com.codeit.monew.useractivity.entity.UserActivity;
import com.codeit.monew.useractivity.exception.UserActivityNotFoundException;
import com.codeit.monew.useractivity.repository.UserActivityRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("활동내역 서비스 테스트")
class UserActivityServiceTest {

  @Mock
  private UserActivityRepository userActivityRepository;

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @InjectMocks
  private UserActivityService userActivityService;

  @Test
  @DisplayName("사용자의 활동내역을 조회")
  void find_returnsUserActivity() {
    UUID userId = UUID.randomUUID();
    UserActivity activity = org.mockito.Mockito.mock(UserActivity.class);
    UserActivityDto expected = new UserActivityDto(
        userId, "user@example.com", "nickname", Instant.now(),
        List.of(), List.of(), List.of(), List.of());
    given(userActivityRepository.findById(userId)).willReturn(Optional.of(activity));
    given(subscriptionRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(userId))
        .willReturn(List.of());
    given(activity.toDto(List.of())).willReturn(expected);

    UserActivityDto result = userActivityService.find(userId);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  @DisplayName("활동내역이 없으면 UserActivityNotFoundException이 발생")
  void find_whenActivityDoesNotExist_throwsException() {
    UUID userId = UUID.randomUUID();
    given(userActivityRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> userActivityService.find(userId))
        .isInstanceOf(UserActivityNotFoundException.class);
    then(subscriptionRepository).shouldHaveNoInteractions();
  }
}
