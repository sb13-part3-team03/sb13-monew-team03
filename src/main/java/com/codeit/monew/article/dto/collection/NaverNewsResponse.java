package com.codeit.monew.article.dto.collection;

import java.util.List;

public record NaverNewsResponse(
        String lastBuildDate,
        long total,
        int start,
        int display,
        List<NaverNewsItem> items
) {
}