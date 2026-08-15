package com.codeit.monew.comment.dto.command;

import java.util.UUID;

public record CommentCreateCommand(
        UUID articleId,
        UUID userId,
        String content
) {
}
