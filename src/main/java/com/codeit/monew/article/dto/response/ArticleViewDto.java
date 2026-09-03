package com.codeit.monew.article.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

public record ArticleViewDto(
        @Schema(description = "기사 조회 ID")
        UUID id,
        @Schema(description = "기사를 조회한 사용자 ID")
        UUID viewedBy,
        @Schema(description = "기사를 본 날짜")
        Instant createdAt,
        @Schema(description = "기사 ID")
        UUID articleId,
        @Schema(description = "출처", allowableValues = {"NAVER", "HANKYUNG", "CHOSUN", "YEONHAP"})
        String source,
        @Schema(description = "원본 기사 URL")
        String sourceUrl,
        @Schema(description = "기사 제목")
        String articleTitle,
        @Schema(description = "기사 발행일")
        Instant articlePublishedDate,
        @Schema(description = "기사 요약")
        String articleSummary,
        @Schema(description = "댓글 수")
        Long articleCommentCount,
        @Schema(description = "조회 수")
        Long articleViewCount
) {
}
