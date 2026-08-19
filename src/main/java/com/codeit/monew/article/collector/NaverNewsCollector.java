package com.codeit.monew.article.collector;

import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import com.codeit.monew.article.dto.collection.NaverNewsItem;
import com.codeit.monew.article.dto.collection.NaverNewsResponse;
import com.codeit.monew.article.entity.ArticleSource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.HtmlUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class NaverNewsCollector implements NewsCollector {

    private final RestClient restClient;

    public NaverNewsCollector(
            @Value("${naver.api.base-url}") String baseUrl,
            @Value("${naver.api.client-id}") String clientId,
            @Value("${naver.api.client-secret}") String clientSecret
    ) {
        ClientHttpRequestFactorySettings settings =
                ClientHttpRequestFactorySettings.defaults()
                        .withTimeouts(
                                Duration.ofSeconds(3),
                                Duration.ofSeconds(5)
                        );

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-NCP-APIGW-API-KEY-ID", clientId)
                .defaultHeader("X-NCP-APIGW-API-KEY", clientSecret)
                .requestFactory(ClientHttpRequestFactoryBuilder.detect()
                        .build(settings)
                )
                .build();
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<CollectedArticleDTO> collect(String keyword) {

        String responseBody = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/v1/news")
                        .queryParam("query", keyword)
                        .queryParam("display", 100)
                        .queryParam("sort", "date")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(String.class);

        if (responseBody == null || responseBody.isBlank()) {
            return List.of();
        }

        NaverNewsResponse response;

        try {
            response = objectMapper.readValue(
                    responseBody,
                    NaverNewsResponse.class
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "네이버 뉴스 API 응답 변환에 실패했습니다.",
                    e
            );
        }

        if (response.items() == null) {
            return List.of();
        }

        return response.items().stream()
                .map(this::toCollectedArticle)
                .flatMap(Optional::stream)
                .toList();
    }

    private Instant parsePublishDate(String pubDate) {
        if (pubDate == null || pubDate.isBlank()) {
            throw new DateTimeParseException(
                    "발행일이 비어 있습니다.",
                    pubDate == null ? "" : pubDate,
                    0
            );
        }
        return ZonedDateTime
                .parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME)
                .toInstant();
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return HtmlUtils.htmlUnescape(text)
                .replace("<b>", "")
                .replace("</b>", "");
    }

    private Optional<CollectedArticleDTO> toCollectedArticle(NaverNewsItem item) {
        try {
            Instant publishDate = parsePublishDate(item.pubDate());

            return Optional.of(new CollectedArticleDTO(
                    ArticleSource.NAVER,
                    item.originallink(),
                    normalizeText(item.title()),
                    normalizeText(item.description()),
                    publishDate
            ));

        } catch (DateTimeParseException e) {
            log.warn(
                    "네이버 뉴스 발행일 파싱 실패로 기사를 건너뜁니다. url={}, pubDate={}",
                    item.originallink(),
                    item.pubDate()
            );

            return Optional.empty();
        }
    }
}