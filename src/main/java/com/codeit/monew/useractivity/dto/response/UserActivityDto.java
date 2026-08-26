package com.codeit.monew.useractivity.dto.response;

import com.codeit.monew.article.dto.response.ArticleViewDto;
import com.codeit.monew.interest.dto.response.SubscriptionDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserActivityDto(
    UUID id,
    String email,
    String nickname,
    Instant createdAt,
    List<SubscriptionDto> subscriptions,
    List<CommentActivityDto> comments,
    List<CommentLikeActivityDto> commentLikes,
    List<ArticleViewDto> articleViews
) {
}
