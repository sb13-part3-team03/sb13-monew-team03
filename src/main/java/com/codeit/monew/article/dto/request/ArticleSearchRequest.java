package com.codeit.monew.article.dto.request;

import com.codeit.monew.article.entity.ArticleSource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ArticleSearchRequest (
        String keyword,
        UUID interestId,
        List<ArticleSource> sourceIn,
        LocalDateTime publishDateFrom,
        LocalDateTime publishDateTo,
        String cursor,
        UUID after,
        String orderBy,
        String direction,
        @Min(1)
        @Max(100)
        Integer limit
) {
    public ArticleSearchRequest {
        if (limit == null) {
            limit = 50;
        }
        if (orderBy == null) {
            orderBy = "publishDate";
        }
        if (direction == null) {
            direction = "asc";
        }
    }
}
