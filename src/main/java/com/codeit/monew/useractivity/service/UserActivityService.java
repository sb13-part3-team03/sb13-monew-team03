package com.codeit.monew.useractivity.service;

import com.codeit.monew.article.dto.response.ArticleViewDto;
import com.codeit.monew.interest.dto.response.SubscriptionDto;
import com.codeit.monew.user.exception.UserNotFoundException;
import com.codeit.monew.useractivity.dto.response.CommentActivityDto;
import com.codeit.monew.useractivity.dto.response.CommentLikeActivityDto;
import com.codeit.monew.useractivity.dto.response.UserActivityDto;
import com.codeit.monew.useractivity.entity.UserActivity;
import com.codeit.monew.useractivity.repository.UserActivityRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityService {

  private final UserActivityRepository userActivityRepository;

  public UserActivityDto find(UUID userId) {
    return findEntity(userId).toDto();
  }

  public void save(UserActivityDto userActivityDto) {
    UserActivity activity = userActivityRepository.findById(userActivityDto.id())
        .orElseGet(() -> new UserActivity(userActivityDto));

    activity.update(userActivityDto);
    userActivityRepository.save(activity);
    log.debug("Saved user activity read model. userId={}", userActivityDto.id());
  }

  public void addSubscription(UUID userId, SubscriptionDto subscription) {
    update(userId, activity -> activity.addSubscription(subscription));
  }

  public void removeSubscription(UUID userId, UUID interestId) {
    update(userId, activity -> activity.removeSubscription(interestId));
  }

  public void addComment(UUID userId, CommentActivityDto comment) {
    update(userId, activity -> activity.addComment(comment));
  }

  public void removeComment(UUID userId, UUID commentId) {
    update(userId, activity -> activity.removeComment(commentId));
  }

  public void addCommentLike(UUID userId, CommentLikeActivityDto commentLike) {
    update(userId, activity -> activity.addCommentLike(commentLike));
  }

  public void removeCommentLike(UUID userId, UUID commentLikeId) {
    update(userId, activity -> activity.removeCommentLike(commentLikeId));
  }

  public void addArticleView(UUID userId, ArticleViewDto articleView) {
    update(userId, activity -> activity.addArticleView(articleView));
  }

  public void delete(UUID userId) {
    userActivityRepository.deleteById(userId);
  }

  private void update(UUID userId, java.util.function.Consumer<UserActivity> updater) {
    UserActivity activity = findEntity(userId);
    updater.accept(activity);
    userActivityRepository.save(activity);
  }

  private UserActivity findEntity(UUID userId) {
    return userActivityRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);
  }

}
