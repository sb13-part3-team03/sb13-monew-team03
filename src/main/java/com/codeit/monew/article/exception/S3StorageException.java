package com.codeit.monew.article.exception;

import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.global.exception.MonewException;

public class S3StorageException extends MonewException {

    public S3StorageException(ErrorCode errorCode) {
        super(errorCode);
    }

}
