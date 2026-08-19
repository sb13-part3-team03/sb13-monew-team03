package com.codeit.monew.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // comment exceptions
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND,"요청된 댓글이 존재하지 않습니다."),
    COMMENT_USER_NOT_FOUND(HttpStatus.NOT_FOUND,"요청된 사용자는 존재하지 않는 사용자 입니다."),
    COMMENT_ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND,"요청된 기사는 존재하지 않는 기사 입니다."),
    COMMENT_INVALID_VALUE(HttpStatus.BAD_REQUEST,"요청 값이 잘못되었습니다."),
    //common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

}
