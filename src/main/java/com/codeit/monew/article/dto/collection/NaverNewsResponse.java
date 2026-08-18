package com.codeit.monew.article.dto.collection;

import java.util.List;

public record NaverNewsResponse(
        List<NaverNewsItem> items
) {
}