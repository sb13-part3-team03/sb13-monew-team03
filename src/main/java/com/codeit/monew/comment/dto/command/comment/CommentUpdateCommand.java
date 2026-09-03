package com.codeit.monew.comment.dto.command.comment;

import java.util.UUID;

public record CommentUpdateCommand(
        UUID commentId,
        String content,
        UUID userId
) {
}
