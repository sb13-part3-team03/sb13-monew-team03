package com.codeit.monew.article.dto.response;

import com.codeit.monew.article.entity.ArticleView;

public record ArticleViewResultDto(
        ArticleView articleView,
        Long commentCount,
        Long viewCount
) {
}
