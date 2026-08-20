package com.codeit.monew.comment.dto.command.comment;

import java.util.UUID;

public record CommentQueryCommand(
        UUID articleId,
        String orderBy,
        String direction,
        String cursor,
        String after,
        Long size,
        UUID requestUserId
) {
}
