package com.codeit.monew.user.exception;

import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.global.exception.MonewException;

public class UserForbiddenException extends MonewException {

    public UserForbiddenException() {
        super(ErrorCode.USER_FORBIDDEN);
    }
}