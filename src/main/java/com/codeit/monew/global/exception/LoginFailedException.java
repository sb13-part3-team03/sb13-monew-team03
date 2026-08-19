package com.codeit.monew.global.exception;

public class LoginFailedException extends MonewException {

    public LoginFailedException() {
        super(ErrorCode.LOGIN_FAILED);
    }
}