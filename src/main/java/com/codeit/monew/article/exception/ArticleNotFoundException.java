package com.codeit.monew.article.exception;

import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.global.exception.MonewException;

public class ArticleNotFoundException extends MonewException {

    public ArticleNotFoundException() {
        super(ErrorCode.ARTICLE_NOT_FOUND);
    }
}