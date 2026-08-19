package com.codeit.monew.interest.exception;

import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.global.exception.MonewException;

public class InterestNotFoundException extends MonewException {
    public InterestNotFoundException() {
        super(ErrorCode.INTEREST_NOT_FOUND);
    }
}
