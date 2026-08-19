package com.codeit.monew.article.collector;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

abstract class RssNewsCollectorTestSupport {

    private HttpServer server;

    protected String feedUrl;

    @BeforeEach
    void startServer() throws IOException {

        server = HttpServer.create(
                new InetSocketAddress(0),
                0
        );

        server.createContext("/rss", exchange -> {

            String rss = """
                    <?xml version="1.0" encoding="UTF-8" ?>
                    <rss version="2.0">
                        <channel>
                            <title>테스트 뉴스</title>

                            <item>
                                <title>AI 기술 발전</title>
                                <link>https://example.com/ai</link>
                                <description>AI 관련 기사입니다.</description>
                                <pubDate>Wed, 19 Aug 2026 10:00:00 +0900</pubDate>
                            </item>

                            <item>
                                <title>오늘의 스포츠</title>
                                <link>https://example.com/sports</link>
                                <description>축구 관련 기사입니다.</description>
                                <pubDate>Wed, 19 Aug 2026 09:00:00 +0900</pubDate>
                            </item>

                        </channel>
                    </rss>
                    """;

            byte[] response =
                    rss.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders()
                    .add(
                            "Content-Type",
                            "application/rss+xml; charset=UTF-8"
                    );

            exchange.sendResponseHeaders(
                    200,
                    response.length
            );

            try (OutputStream outputStream =
                         exchange.getResponseBody()) {

                outputStream.write(response);
            }
        });

        server.start();

        feedUrl = "http://localhost:"
                + server.getAddress().getPort()
                + "/rss";
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }
}