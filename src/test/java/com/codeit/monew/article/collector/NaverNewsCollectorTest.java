package com.codeit.monew.article.collector;

import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import com.codeit.monew.article.entity.ArticleSource;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NaverNewsCollectorTest {

    private HttpServer server;
    private NaverNewsCollector collector;

    private String responseBody;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(
                new InetSocketAddress(0),
                0
        );

        server.createContext("/search/v1/news", exchange -> {
            byte[] response = responseBody == null
                    ? new byte[0]
                    : responseBody.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders()
                    .add("Content-Type", "application/json; charset=UTF-8");

            exchange.sendResponseHeaders(200, response.length);

            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        });

        server.start();

        String baseUrl =
                "http://localhost:" + server.getAddress().getPort();

        collector = new NaverNewsCollector(
                baseUrl,
                "test-client-id",
                "test-client-secret"
        );
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    @DisplayName("네이버 뉴스 응답을 DTO로 변환한다.")
    void collect_mapsResponseToDto() {
        // given
        responseBody = """
                {
                  "lastBuildDate": "Wed, 19 Aug 2026 10:00:00 +0900",
                  "total": 1,
                  "start": 1,
                  "display": 1,
                  "items": [
                    {
                      "title": "<b>AI</b> &quot;기술&quot; 발전",
                      "originallink": "https://example.com/article/1",
                      "link": "https://example.com/article/1",
                      "description": "<b>AI</b> 관련 &quot;뉴스&quot;입니다.",
                      "pubDate": "Wed, 19 Aug 2026 10:00:00 +0900"
                    }
                  ]
                }
                """;

        // when
        List<CollectedArticleDTO> articles =
                collector.collect("AI");

        // then
        assertThat(articles).hasSize(1);

        CollectedArticleDTO article = articles.get(0);

        assertThat(article.source())
                .isEqualTo(ArticleSource.NAVER);

        assertThat(article.sourceUrl())
                .isEqualTo("https://example.com/article/1");

        assertThat(article.title())
                .isEqualTo("AI \"기술\" 발전");

        assertThat(article.summary())
                .isEqualTo("AI 관련 \"뉴스\"입니다.");

        assertThat(article.publishDate())
                .isNotNull();
    }

    @Test
    @DisplayName("응답 본문이 비어 있으면 빈 목록을 반환한다.")
    void collect_whenResponseBodyIsBlank_returnsEmptyList() {
        // given
        responseBody = "";

        // when
        List<CollectedArticleDTO> articles =
                collector.collect("AI");

        // then
        assertThat(articles).isEmpty();
    }

    @Test
    @DisplayName("items가 null이면 빈 목록을 반환한다.")
    void collect_whenItemsIsNull_returnsEmptyList() {
        // given
        responseBody = """
                {
                  "lastBuildDate": "Wed, 19 Aug 2026 10:00:00 +0900",
                  "total": 0,
                  "start": 1,
                  "display": 0,
                  "items": null
                }
                """;

        // when
        List<CollectedArticleDTO> articles =
                collector.collect("AI");

        // then
        assertThat(articles).isEmpty();
    }

    @Test
    @DisplayName("description이 null이면 빈 문자열로 변환한다.")
    void collect_whenDescriptionIsNull_convertsToEmptyString() {
        // given
        responseBody = """
                {
                  "lastBuildDate": "Wed, 19 Aug 2026 10:00:00 +0900",
                  "total": 1,
                  "start": 1,
                  "display": 1,
                  "items": [
                    {
                      "title": "AI 뉴스",
                      "originallink": "https://example.com/article/1",
                      "link": "https://example.com/article/1",
                      "description": null,
                      "pubDate": "Wed, 19 Aug 2026 10:00:00 +0900"
                    }
                  ]
                }
                """;

        // when
        List<CollectedArticleDTO> articles =
                collector.collect("AI");

        // then
        assertThat(articles).hasSize(1);
        assertThat(articles.get(0).summary()).isEmpty();
    }

    @Test
    @DisplayName("발행일 형식이 잘못된 기사는 건너뛴다.")
    void collect_whenPublishDateIsMalformed_skipsArticle() {
        // given
        responseBody = """
                {
                  "lastBuildDate": "Wed, 19 Aug 2026 10:00:00 +0900",
                  "total": 1,
                  "start": 1,
                  "display": 1,
                  "items": [
                    {
                      "title": "AI 뉴스",
                      "originallink": "https://example.com/article/1",
                      "link": "https://example.com/article/1",
                      "description": "AI 관련 뉴스입니다.",
                      "pubDate": "잘못된 날짜"
                    }
                  ]
                }
                """;

        // when
        List<CollectedArticleDTO> articles =
                collector.collect("AI");

        // then
        assertThat(articles).isEmpty();
    }
}