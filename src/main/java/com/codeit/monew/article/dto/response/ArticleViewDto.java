package com.codeit.monew.article.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ArticleViewDto(
        UUID id,
        UUID viewedBy,
        Instant createdAt,
        UUID articleId,
        String source,
        String sourceUrl,
        String articleTitle,
        Instant articlePublishedDate,
        String articleSummary,
        Long articleCommentCount,
        Long articleViewCount
) {
}
