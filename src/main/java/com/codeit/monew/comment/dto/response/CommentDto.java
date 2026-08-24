package com.codeit.monew.comment.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CommentDto(
        UUID id,
        UUID articleId,
        UUID userId,
        String userNickname,
        String content,
        Long likeCount,
        Boolean likeByMe,
        Instant createdAt
        ) {
}
