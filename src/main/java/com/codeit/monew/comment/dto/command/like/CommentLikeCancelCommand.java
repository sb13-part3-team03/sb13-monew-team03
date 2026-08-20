package com.codeit.monew.comment.dto.command.like;

import java.util.UUID;

public record CommentLikeCancelCommand(
        UUID commentId,
        UUID userId
) {
}
