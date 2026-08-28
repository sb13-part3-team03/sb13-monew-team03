package com.codeit.monew.article.dto.response;

import com.codeit.monew.article.entity.Article;

public record ArticleSearchResultDto(
        Article article,
        Long commentCount,
        Long viewCount,
        Boolean viewedByMe
) {
}
