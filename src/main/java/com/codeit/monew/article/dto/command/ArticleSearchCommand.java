package com.codeit.monew.article.dto.command;

import com.codeit.monew.article.dto.request.ArticleSearchRequest;
import com.codeit.monew.article.entity.ArticleSource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record ArticleSearchCommand(
        String keyword,
        UUID interestId,
        ArticleSource sourceIn,
        Instant publishDateFrom,
        Instant publishDateTo,
        String cursor,
        UUID after,
        String orderBy,
        String direction,
        Integer limit,
        UUID userId
) {

    public static ArticleSearchCommand from(
            ArticleSearchRequest request,
            UUID userId
    ) {
        return new ArticleSearchCommand(
                request.keyword(),
                request.interestId(),
                request.sourceIn(),
                // api(LocalDateTime) -> 데이터(Instant)로 변경
                toInstant(request.publishDateFrom()),
                toInstant(request.publishDateTo()),
                request.cursor(),
                request.after(),
                request.orderBy(),
                request.direction(),
                request.limit(),
                userId
        );
    }

    private static Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null
                ? null
                : dateTime.toInstant(ZoneOffset.UTC);
    }

}
