package com.codeit.monew.global.exception;

import lombok.Getter;

@Getter
public class MonewException extends RuntimeException {

    private final ErrorCode errorCode;

    public MonewException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public MonewException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
