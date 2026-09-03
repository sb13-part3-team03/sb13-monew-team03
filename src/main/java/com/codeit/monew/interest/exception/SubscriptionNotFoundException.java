package com.codeit.monew.interest.exception;

import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.global.exception.MonewException;

public class SubscriptionNotFoundException extends MonewException {
    public SubscriptionNotFoundException() {
        super(ErrorCode.SUBSCRIPTION_NOT_FOUND);
    }
}
