package com.codeit.monew.comment.service;

import com.codeit.monew.comment.dto.command.CommentCreateCommand;
import com.codeit.monew.comment.dto.command.CommentQueryCommand;
import com.codeit.monew.comment.dto.response.CommentDto;
import com.codeit.monew.comment.dto.response.CursorContainerDto;

public interface CommentService {
    CommentDto registry(CommentCreateCommand command);
    CursorContainerDto<CommentDto> query(CommentQueryCommand command);
}
