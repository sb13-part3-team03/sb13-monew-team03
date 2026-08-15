package com.codeit.monew.comment.service;

import com.codeit.monew.comment.dto.command.CommentCreateCommand;
import com.codeit.monew.comment.dto.response.CommentDto;

public interface CommentService {
    CommentDto registry(CommentCreateCommand command);
}
