package com.codeit.monew.article.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ArticleSource {

    NAVER("네이버"),
    HANKYUNG("한국경제"),
    CHOSUN("조선일보"),
    YONHAP("연합뉴스");

    private final String description;

}