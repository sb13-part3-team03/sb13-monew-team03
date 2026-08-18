package com.codeit.monew.article.collector;

import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import com.codeit.monew.article.dto.collection.NaverNewsResponse;
import com.codeit.monew.article.entity.ArticleSource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class NaverNewsCollector implements NewsCollector {

    private final RestClient restClient;

    public NaverNewsCollector(
            @Value("${naver.api.base-url}") String baseUrl,
            @Value("${naver.api.client-id}") String clientId,
            @Value("${naver.api.client-secret}") String clientSecret
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-NCP-APIGW-API-KEY-ID", clientId)
                .defaultHeader("X-NCP-APIGW-API-KEY", clientSecret)
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
                .map(item -> new CollectedArticleDTO(
                        ArticleSource.NAVER,
                        item.originallink(),
                        item.title(),
                        item.description() == null ? "" : item.description(),
                        parsePublishDate(item.pubDate())
                ))
                .toList();
    }

    private Instant parsePublishDate(String pubDate) {
        return ZonedDateTime
                .parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME)
                .toInstant();
    }
}