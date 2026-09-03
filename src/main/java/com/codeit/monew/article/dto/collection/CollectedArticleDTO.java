package com.codeit.monew.article.dto.collection;

import com.codeit.monew.article.entity.ArticleSource;

import java.time.Instant;

public record CollectedArticleDTO(
        ArticleSource source,
        String sourceUrl,
        String title,
        String summary,
        Instant publishDate
) {
}
