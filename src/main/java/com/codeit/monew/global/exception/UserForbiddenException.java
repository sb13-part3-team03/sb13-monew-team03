package com.codeit.monew.global.exception;

public class UserForbiddenException extends MonewException {

    public UserForbiddenException() {
        super(ErrorCode.USER_FORBIDDEN);
    }
}