package com.codeit.monew.article.dto.response;

import com.codeit.monew.article.entity.ArticleSource;

import java.time.Instant;
import java.util.UUID;

public record ArticleDto (
        UUID id,
        ArticleSource source,
        String sourceUrl,
        String title,
        Instant publishDate,
        String summary,
        Integer commentCount,
        Integer viewCount,
        Boolean viewedByMe
) {
}
