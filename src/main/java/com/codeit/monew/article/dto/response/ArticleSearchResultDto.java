package com.codeit.monew.article.dto.response;

import com.codeit.monew.article.entity.Article;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "뉴스 기사 검색 결과")
public record ArticleSearchResultDto(
        @Schema(description = "기사")
        Article article,
        @Schema(description = "댓글 수")
        Long commentCount,
        @Schema(description = "조회 수")
        Long viewCount,
        @Schema(description = "현재 사용자의 기사 조회 여부")
        Boolean viewedByMe
) {
}
