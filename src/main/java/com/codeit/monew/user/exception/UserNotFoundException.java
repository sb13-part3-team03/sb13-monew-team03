package com.codeit.monew.user.exception;

import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.global.exception.MonewException;

public class UserNotFoundException extends MonewException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}