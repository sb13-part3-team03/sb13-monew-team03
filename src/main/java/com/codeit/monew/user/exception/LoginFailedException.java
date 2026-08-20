package com.codeit.monew.user.exception;

import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.global.exception.MonewException;

public class LoginFailedException extends MonewException {

    public LoginFailedException() {
        super(ErrorCode.LOGIN_FAILED);
    }
}