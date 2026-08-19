package com.codeit.monew.interest.exception;

import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.global.exception.MonewException;

public class AlreadySubscribedException extends MonewException {
    public AlreadySubscribedException() {
        super(ErrorCode.ALREADY_SUBSCRIBED);
    }
}
