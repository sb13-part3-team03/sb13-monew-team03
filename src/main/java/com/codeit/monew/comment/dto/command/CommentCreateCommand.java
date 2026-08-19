package com.codeit.monew.comment.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CommentCreateCommand(
        @NotNull UUID articleId,
        @NotNull UUID userId,
        @NotBlank String content
) {
}
