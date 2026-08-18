package com.codeit.monew.comment.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentLikeDto(
        UUID id,
        UUID likeBy,
        LocalDateTime createdAt,
        UUID commentId,
        UUID articleId,
        UUID commentUserId,
        String commentUserNickName,
        String commentContent,
        Long commentLikeCount,
        LocalDateTime commentCreateAt
) {
}
