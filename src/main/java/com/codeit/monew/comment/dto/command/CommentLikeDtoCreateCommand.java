package com.codeit.monew.comment.dto.command;

import java.time.Instant;
import java.util.UUID;

public record CommentLikeDtoCreateCommand (
        UUID id,
        UUID likeBy,
        Instant createdAt,
        UUID commentId,
        UUID articleId,
        UUID commentUserId,
        String commentUserNickName,
        String commentContent,
        Long commentLikeCount,
        Instant commentCreateAt
) {
}
