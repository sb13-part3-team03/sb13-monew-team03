package com.codeit.monew.article.dto.request;

import com.codeit.monew.article.entity.ArticleSource;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "뉴스 기사 검색 조건 및 필터")
public record ArticleSearchRequest (
        @Schema(description = "검색어(제목, 요약)", example = "스포츠")
        String keyword,

        @Schema(description = "관심사 ID", example = "스포츠")
        UUID interestId,

        @Schema(description = "출처(포함)", allowableValues = {"NAVER", "HANKYUNG", "CHOSUN", "YEONHAP"})
        List<ArticleSource> sourceIn,

        @Schema(description = "날짜 시작(범위) (LocalDateTime)")
        LocalDateTime publishDateFrom,

        @Schema(description = "날짜 끝(범위) (LocalDateTime)")
        LocalDateTime publishDateTo,

        @Schema(description = "커서 값")
        String cursor,

        @Schema(description = "보조 커서(createdAt) 값")
        UUID after,

        @Schema(description = "정렬 속성 이름", allowableValues = {"publishDate", "commentCount", "viewCount"})
        String orderBy,

        @Schema(description = "정렬 방향 (ASC, DESC)", allowableValues = {"ASC", "DESC"})
        String direction,

        @Schema(description = "커서 페이지 크기", example = "50")
        @Min(1)
        @Max(100)
        Integer limit
) {
    public ArticleSearchRequest {
        if (limit == null) {
            limit = 50;
        }
        if (orderBy == null) {
            orderBy = "publishDate";
        }
        if (direction == null) {
            direction = "desc";
        }
    }
}
