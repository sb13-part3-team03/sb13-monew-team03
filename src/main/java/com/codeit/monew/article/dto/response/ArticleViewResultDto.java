package com.codeit.monew.article.dto.response;

import com.codeit.monew.article.entity.ArticleView;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기사 조회 기록 및 통계 정보")
public record ArticleViewResultDto(
        @Schema(description = "기사 조회 기록 정보")
        ArticleView articleView,
        @Schema(description = "댓글 수")
        Long commentCount,
        @Schema(description = "조회 수")
        Long viewCount
) {
}
