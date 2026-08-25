package com.codeit.monew.article.dto.response;

import java.time.Instant;
import java.util.List;

public record ArticleRestoreResultDto (
        Instant restoreDate,
        List<String> restoredArticleIds,
        Long restoredArticleCount
) {
}
