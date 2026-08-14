package com.codeit.monew.comment.exception;

import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.global.exception.MonewException;

public class CommentException extends MonewException {
    public CommentException(ErrorCode code) {
        super(code);
    }
}
