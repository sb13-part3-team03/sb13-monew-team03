package com.codeit.monew.global.exception;

public class UserNotFoundException extends MonewException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}