package com.codeit.monew.article.dto.response;

import java.util.List;
import java.util.UUID;

public record CursorPageResponseArticleDto (
        List<ArticleDto> content,
        String nextCursor,
        Long nextAfter,
        Integer size,
        Long totalElements,
        Boolean hasNext
) {
}
