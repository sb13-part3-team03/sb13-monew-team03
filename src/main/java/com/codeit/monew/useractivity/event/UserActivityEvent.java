package com.codeit.monew.useractivity.event;

import com.codeit.monew.article.dto.response.ArticleViewDto;
import com.codeit.monew.useractivity.dto.response.CommentActivityDto;
import com.codeit.monew.useractivity.dto.response.CommentLikeActivityDto;
import java.time.Instant;
import java.util.UUID;

/** Immutable snapshots only: listeners must not access managed JPA entities after commit. */
public interface UserActivityEvent {
  record Profile(UUID id, String email, String nickname, Instant createdAt)
      implements UserActivityEvent {}
  record CommentAdded(Profile user, CommentActivityDto comment) implements UserActivityEvent {}
  record CommentUpdated(UUID commentId, String content) implements UserActivityEvent {}
  record CommentRemoved(UUID commentId) implements UserActivityEvent {}
  record LikeAdded(Profile user, CommentLikeActivityDto like) implements UserActivityEvent {}
  record LikeRemoved(UUID userId, UUID likeId) implements UserActivityEvent {}
  record LikeCountChanged(UUID commentId, long count) implements UserActivityEvent {}
  record ArticleViewed(Profile user, ArticleViewDto view) implements UserActivityEvent {}
  record UserRemoved(UUID userId) implements UserActivityEvent {}
}
