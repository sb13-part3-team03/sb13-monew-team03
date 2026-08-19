package com.codeit.monew.article.dto.command;

import com.codeit.monew.article.dto.request.ArticleSearchRequest;
import com.codeit.monew.article.entity.ArticleSource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

public record ArticleSearchCommand(
        String keyword,
        UUID interestId,
        List<ArticleSource> sourceIn,
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

    // API의 LocalDateTime을 내부에서 사용하는 Instant로 변환
    private static Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null
                ? null
                : dateTime.toInstant(ZoneOffset.UTC);
    }

    // 레포지토리 테스트용 메서드
    private ArticleSearchCommand createSearchCommand (
            String keyword,
            UUID interestId,
            List<ArticleSource> sourceIn,
            Instant publishDateFrom,
            Instant publishDateTo
    ) {
        return new ArticleSearchCommand(
                keyword,
                interestId,
                sourceIn,
                publishDateFrom,
                publishDateTo,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

}
