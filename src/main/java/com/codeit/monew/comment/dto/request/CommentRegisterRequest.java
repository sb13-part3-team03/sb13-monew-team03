package com.codeit.monew.comment.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CommentRegisterRequest(
        @NotNull UUID articleId,
        @NotNull UUID userId,
        @NotNull String content
) {
}
