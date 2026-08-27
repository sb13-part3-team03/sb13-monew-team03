package com.codeit.monew.useractivity.event;

import com.codeit.monew.article.dto.response.ArticleViewDto;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentLike;
import com.codeit.monew.comment.repository.CommentLikeRepository;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.useractivity.dto.response.CommentActivityDto;
import com.codeit.monew.useractivity.dto.response.CommentLikeActivityDto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserActivityEventPublisher {
  private final ApplicationEventPublisher events;
  private final CommentLikeRepository likes;

  public void profileChanged(User user) {
    events.publishEvent(profile(user));
  }

  public void commentAdded(Comment comment, long likeCount) {
    User user = comment.getUser();
    events.publishEvent(new UserActivityEvent.CommentAdded(profile(user),
        new CommentActivityDto(comment.getId(), comment.getArticle().getId(),
            comment.getArticle().getTitle(), user.getId(), user.getNickname(),
            comment.getContent(), likeCount, comment.getCreatedAt())));
  }

  public void commentUpdated(Comment comment) {
    events.publishEvent(new UserActivityEvent.CommentUpdated(comment.getId(), comment.getContent()));
  }

  public void commentRemoved(UUID commentId) {
    events.publishEvent(new UserActivityEvent.CommentRemoved(commentId));
  }

  public void likeAdded(CommentLike like, long count) {
    Comment comment = like.getComment();
    events.publishEvent(new UserActivityEvent.LikeAdded(profile(like.getUser()),
        new CommentLikeActivityDto(like.getId(), like.getCreatedAt(), comment.getId(),
            comment.getArticle().getId(), comment.getArticle().getTitle(),
            comment.getUser().getId(), comment.getUser().getNickname(),
            comment.getContent(), count, comment.getCreatedAt())));
    events.publishEvent(new UserActivityEvent.LikeCountChanged(comment.getId(), count));
  }

  public void likeRemoved(UUID userId, UUID likeId, UUID commentId) {
    events.publishEvent(new UserActivityEvent.LikeRemoved(userId, likeId));
    events.publishEvent(new UserActivityEvent.LikeCountChanged(commentId,
        likes.countByComment_Id(commentId)));
  }

  public void articleViewed(User user, ArticleViewDto view) {
    events.publishEvent(new UserActivityEvent.ArticleViewed(profile(user), view));
  }

  public void userRemoved(UUID userId) {
    events.publishEvent(new UserActivityEvent.UserRemoved(userId));
  }

  private UserActivityEvent.Profile profile(User user) {
    return new UserActivityEvent.Profile(user.getId(), user.getEmail(),
        user.getNickname(), user.getCreatedAt());
  }
}
