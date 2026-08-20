package com.codeit.monew.comment.service;

import com.codeit.monew.comment.dto.command.comment.CommentCreateCommand;
import com.codeit.monew.comment.dto.command.comment.CommentQueryCommand;
import com.codeit.monew.comment.dto.command.comment.CommentUpdateCommand;
import com.codeit.monew.comment.dto.response.CommentDto;
import com.codeit.monew.comment.dto.response.CursorContainerDto;

import java.util.UUID;

public interface CommentService {
    CommentDto registry(CommentCreateCommand command);
    CursorContainerDto<CommentDto> query(CommentQueryCommand command);
    CommentDto update(CommentUpdateCommand command);
    void mask(UUID commentId);
    void delete(UUID commentId);
}
