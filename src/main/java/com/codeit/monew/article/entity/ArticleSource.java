package com.codeit.monew.article.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ArticleSource {

    NAVER("네이버"),
    HANKYUNG("한국경제"),
    CHOSUN("조선일보"),
    YEONHAP("연합뉴스");

    @JsonCreator
    public static ArticleSource from(String value) {
        if ("YONHAP".equals(value)) {
            // 기존 YONHAP으로 저장된 데이터와의 호환성을 유지
            return YEONHAP;
        }

        return ArticleSource.valueOf(value);
    }

    private final String description;

}