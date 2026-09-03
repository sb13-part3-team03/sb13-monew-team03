package com.codeit.monew.comment.dto.command.like;

import java.util.UUID;

public record CommentLikeRegistryCommand (
        UUID commentId,
        UUID userId
) {
}
