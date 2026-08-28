package com.codeit.monew.article.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "뉴스 기사 복구 결과")
public record ArticleRestoreResultDto (
        @Schema(description = "기사 복구 일시")
        Instant restoreDate,

        @Schema(description = "복구된 기사 ID 목록")
        List<UUID> restoredArticleIds,

        @Schema(description = "복구된 기사 수")
        Long restoredArticleCount
) {
}
