package com.codeit.monew.article.exception;

import com.codeit.monew.global.exception.ErrorCode;

public class S3StorageException extends RuntimeException {

    public S3StorageException(ErrorCode errorCode) {
        super(errorCode.getMessage());
    }

}
