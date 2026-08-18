package com.codeit.monew.comment.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDto(
        UUID id,
        UUID articleId,
        UUID userId,
        String userNickName,
        String content,
        Long likeCount,
        Boolean likeByMe,
        LocalDateTime createdAt
        ) {
}
