package com.codeit.monew.global.exception;

public class DuplicateEmailException extends MonewException {

    public DuplicateEmailException() {
        super(ErrorCode.DUPLICATE_EMAIL);
    }
}