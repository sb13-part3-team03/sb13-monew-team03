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

  @Id private UUID id;
  @CreatedDate private Instant createdAt;
  @LastModifiedDate private Instant updatedAt;
  private String email;
  private String nickname;
  private List<CommentActivityDto> comments;
  private List<CommentLikeActivityDto> commentLikes;
  private List<ArticleViewDto> articleViews;


  public UserActivity(UserActivityDto dto) {
    this.id = dto.id();
    this.email = dto.email();
    this.nickname = dto.nickname();
    this.createdAt = dto.createdAt();
    this.comments = mutableCopy(dto.comments());
    this.commentLikes = mutableCopy(dto.commentLikes());
    this.articleViews = mutableCopy(dto.articleViews());
  }

  public UserActivityDto toDto(List<SubscriptionDto> subscriptions) {
    return new UserActivityDto(
        id, email, nickname, createdAt,
        List.copyOf(subscriptions),
        List.copyOf(comments),
        List.copyOf(commentLikes),
        List.copyOf(articleViews)
    );
  }

  private static <T> ArrayList<T> mutableCopy(List<T> source) {
    return source == null ? new ArrayList<>() : new ArrayList<>(source);
  }

}
