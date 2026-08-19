package com.codeit.monew.article.dto.response;

import com.codeit.monew.article.entity.Article;

public record ArticleSearchResult(
        Article article,
        Long commentCount,
        Long viewCount,
        Boolean viewedByMe
) {
}
