package com.codeit.monew.article.dto.command;

import com.codeit.monew.article.dto.request.ArticleSearchRequest;
import com.codeit.monew.article.entity.ArticleSource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public record ArticleSearchCommand(
        String keyword,
        UUID interestId,
        List<ArticleSource> sourceIn,
        Instant publishDateFrom,
        Instant publishDateTo,
        String cursor,
        Instant after,
        UUID afterId,
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
                toInstant(request.publishDateFrom()),
                toInstant(request.publishDateTo()),
                request.cursor(),
                request.after(),
                request.afterId(),
                request.orderBy(),
                request.direction(),
                request.limit(),
                userId
        );
    }

    // API의 LocalDateTime을 내부에서 사용하는 Instant로 변환
    private static Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null
                ? null
                : dateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }

}
