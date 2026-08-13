package com.codeit.monew.article.dto.request;

import com.codeit.monew.article.entity.ArticleSource;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleSearchRequest (
        String keyword,
        UUID interestId,
        ArticleSource sourceIn,
        LocalDateTime publishDateFrom,
        LocalDateTime publishDateTo
) {
}
