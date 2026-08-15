package com.codeit.monew.comment.repository.querydsl;

import com.codeit.monew.comment.dto.command.CommentDtoCreateCommand;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepositoryDsl {
    /**
     * return command what can convert CommentDto
     * @param commentId - UUID. comment entity identifier
     * @return Optional<CommentDtoCreateCommand></CommentDtoCreateCommand>
     */
    Optional<CommentDtoCreateCommand> getDtoCommandById(UUID commentId);
}
