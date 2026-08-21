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
    ),

    // article
    ARTICLE_NOT_FOUND(
          HttpStatus.NOT_FOUND,
          "뉴스 기사 정보를 찾을 수 없습니다."
    ),

    // comment exceptions
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND,"요청된 댓글이 존재하지 않습니다."),
    COMMENT_USER_NOT_FOUND(HttpStatus.NOT_FOUND,"요청된 사용자는 존재하지 않는 사용자 입니다."),
    COMMENT_ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND,"요청된 기사는 존재하지 않는 기사 입니다."),
    COMMENT_INVALID_VALUE(HttpStatus.BAD_REQUEST,"요청 값이 잘못되었습니다."),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN,"댓글에 대한 권한이 없습니다."),

    COMMENT_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND,"좋아요가 눌리지 않은 상태 입니다."),
    COMMENT_LIKE_ALREADY_EXISTED(HttpStatus.CONFLICT,"이미 좋아요가 눌린 상태 입니다."),

    // interest
    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "관심사 정보를 찾을 수 없습니다."),
    ALREADY_SUBSCRIBED(HttpStatus.CONFLICT, "이미 구독 중인 관심사입니다."),
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "구독 정보를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }



}
