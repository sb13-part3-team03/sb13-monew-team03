package com.codeit.monew.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // common
    INVALID_INPUT_VALUE(
            HttpStatus.BAD_REQUEST,
            "입력값이 올바르지 않습니다."
    ),
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 내부 오류가 발생했습니다."
    ),

    // user
    DUPLICATE_EMAIL(
            HttpStatus.CONFLICT,
            "이미 사용 중인 이메일입니다."
    ),
    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "사용자를 찾을 수 없습니다."
    ),
    USER_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "사용자 정보 수정 권한이 없습니다."
    ),
    LOGIN_FAILED(
            HttpStatus.UNAUTHORIZED,
            "이메일 또는 비밀번호가 일치하지 않습니다."
    ),

    // notification
    NOTIFICATION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "알림을 찾을 수 없습니다."
    );

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
