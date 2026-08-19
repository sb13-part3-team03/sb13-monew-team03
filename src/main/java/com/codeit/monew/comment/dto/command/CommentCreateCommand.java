package com.codeit.monew.comment.dto.command;

import com.codeit.monew.comment.dto.request.CommentRegisterRequest;

import java.util.UUID;

public record CommentCreateCommand(
        UUID articleId,
        UUID userId,
        String content
) {}
