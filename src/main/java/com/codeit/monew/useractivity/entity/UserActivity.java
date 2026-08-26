package com.codeit.monew.useractivity.entity;

import com.codeit.monew.article.dto.response.ArticleViewDto;
import com.codeit.monew.interest.dto.response.SubscriptionDto;
import com.codeit.monew.useractivity.dto.response.CommentActivityDto;
import com.codeit.monew.useractivity.dto.response.CommentLikeActivityDto;
import com.codeit.monew.useractivity.dto.response.UserActivityDto;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_activities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserActivity {

  private static final int RECENT_ACTIVITY_LIMIT = 10;

  @Id private UUID id;
  @CreatedDate private Instant createdAt;
  @LastModifiedDate private Instant updatedAt;
  private String email;
  private String nickname;
  private List<SubscriptionDto> subscriptions;
  private List<CommentActivityDto> comments;
  private List<CommentLikeActivityDto> commentLikes;
  private List<ArticleViewDto> articleViews;


  public UserActivity(UserActivityDto dto) {
    update(dto);
  }

  public void update(UserActivityDto dto) {
    this.id = dto.id();
    this.email = dto.email();
    this.nickname = dto.nickname();
    this.createdAt = dto.createdAt();
    this.subscriptions = mutableCopy(dto.subscriptions());
    this.comments = recentCopy(dto.comments());
    this.commentLikes = recentCopy(dto.commentLikes());
    this.articleViews = recentCopy(dto.articleViews());
  }

  public void addSubscription(SubscriptionDto subscription) {
    subscriptions.removeIf(item -> item.interestId().equals(subscription.interestId()));
    subscriptions.add(subscription);
  }

  public void removeSubscription(UUID interestId) {
    subscriptions.removeIf(item -> item.interestId().equals(interestId));
  }

  public void addComment(CommentActivityDto comment) {
    comments.removeIf(item -> item.id().equals(comment.id()));
    addRecent(comments, comment);
  }

  public void removeComment(UUID commentId) {
    comments.removeIf(item -> item.id().equals(commentId));
  }

  public void addCommentLike(CommentLikeActivityDto commentLike) {
    commentLikes.removeIf(item -> item.id().equals(commentLike.id()));
    addRecent(commentLikes, commentLike);
  }

  public void removeCommentLike(UUID commentLikeId) {
    commentLikes.removeIf(item -> item.id().equals(commentLikeId));
  }

  public void addArticleView(ArticleViewDto articleView) {
    articleViews.removeIf(item -> item.articleId().equals(articleView.articleId()));
    addRecent(articleViews, articleView);
  }

  public UserActivityDto toDto() {
    return new UserActivityDto(
        id, email, nickname, createdAt,
        List.copyOf(subscriptions),
        List.copyOf(comments),
        List.copyOf(commentLikes),
        List.copyOf(articleViews)
    );
  }

  private <T> void addRecent(List<T> activities, T activity) {
    activities.add(0, activity);
    if (activities.size() > RECENT_ACTIVITY_LIMIT) {
      activities.remove(activities.size() - 1);
    }
  }

  private static <T> ArrayList<T> mutableCopy(List<T> source) {
    return source == null ? new ArrayList<>() : new ArrayList<>(source);
  }

  private static <T> ArrayList<T> recentCopy(List<T> source) {
    ArrayList<T> copy = mutableCopy(source);
    if (copy.size() > RECENT_ACTIVITY_LIMIT) {
      return new ArrayList<>(copy.subList(0, RECENT_ACTIVITY_LIMIT));
    }
    return copy;
  }

}
