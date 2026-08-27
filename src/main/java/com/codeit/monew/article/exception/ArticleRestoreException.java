package com.codeit.monew.article.exception;

import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.global.exception.MonewException;

public class ArticleRestoreException extends MonewException {

    public ArticleRestoreException(ErrorCode errorCode) {
        super(errorCode);
    }

}
