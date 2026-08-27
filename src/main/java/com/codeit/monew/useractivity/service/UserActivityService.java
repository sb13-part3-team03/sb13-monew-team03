package com.codeit.monew.useractivity.service;

import com.codeit.monew.interest.dto.response.SubscriptionDto;
import com.codeit.monew.interest.entity.Subscription;
import com.codeit.monew.interest.repository.SubscriptionRepository;
import com.codeit.monew.user.exception.UserNotFoundException;
import com.codeit.monew.useractivity.dto.response.UserActivityDto;
import com.codeit.monew.useractivity.entity.UserActivity;
import com.codeit.monew.useractivity.repository.UserActivityRepository;
import java.util.UUID;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserActivityService {

  private final UserActivityRepository userActivityRepository;
  private final SubscriptionRepository subscriptionRepository;

  @Transactional(readOnly = true)
  public UserActivityDto find(UUID userId) {
    UserActivity activity = userActivityRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);
    return activity.toDto(findSubscriptions(userId));
  }

  private List<SubscriptionDto> findSubscriptions(UUID userId) {
    // The frontend uses the full array length for the total, then displays its first 10 items.
    List<Subscription> subscriptions =
        subscriptionRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(userId);
    if (subscriptions.isEmpty()) {
      return List.of();
    }

    return subscriptions.stream()
        .map(subscription -> SubscriptionDto.from(subscription,
            subscriptionRepository.countByInterestId(subscription.getInterest().getId())))
        .toList();
  }

}
