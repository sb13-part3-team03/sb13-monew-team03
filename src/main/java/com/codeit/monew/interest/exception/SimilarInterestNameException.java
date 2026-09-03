package com.codeit.monew.interest.exception;

import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.global.exception.MonewException;

public class SimilarInterestNameException extends MonewException {
    public SimilarInterestNameException() {
        super(ErrorCode.SIMILAR_INTEREST_NAME);
    }
}
