package com.codeit.monew.article.dto.collection;

public record NaverNewsItem(
        String title,
        String originallink,
        String link,
        String description,
        String pubDate
) {
}