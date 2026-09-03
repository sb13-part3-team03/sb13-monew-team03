package com.codeit.monew.article.dto.response;

import com.codeit.monew.article.entity.ArticleSource;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record ArticleDto (
        @Schema(description = "기사 ID")
        UUID id,

        @Schema(description = "출처", allowableValues = {"NAVER", "HANKYUNG", "CHOSUN", "YEONHAP"})
        ArticleSource source,

        @Schema(description = "원본 출처 URL")
        String sourceUrl,

        @Schema(description = "기사 제목")
        String title,

        @Schema(description = "기사 발행일")
        Instant publishDate,

        @Schema(description = "기사 요약")
        String summary,

        @Schema(description = "댓글 수")
        Integer commentCount,

        @Schema(description = "조회 수")
        Integer viewCount,

        @Schema(description = "현재 사용자의 기사 조회 여부")
        Boolean viewedByMe
) {
}
