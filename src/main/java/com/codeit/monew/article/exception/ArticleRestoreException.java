package com.codeit.monew.article.exception;

import com.codeit.monew.global.exception.ErrorCode;

public class ArticleRestoreException extends RuntimeException {

    public ArticleRestoreException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
    }

}
