package com.codeit.monew.comment.service;

import com.codeit.monew.comment.dto.command.like.CommentLikeCancelCommand;
import com.codeit.monew.comment.dto.command.like.CommentLikeRegistryCommand;
import com.codeit.monew.comment.dto.response.CommentLikeDto;

public interface CommentLikeService {
    CommentLikeDto registry(CommentLikeRegistryCommand command);
    void cancel(CommentLikeCancelCommand command);
}
