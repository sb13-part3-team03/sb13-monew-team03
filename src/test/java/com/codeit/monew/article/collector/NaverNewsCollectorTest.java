package com.codeit.monew.article.collector;

import com.codeit.monew.article.collector.NaverNewsCollector;
import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

class NaverNewsCollectorTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(NaverNewsCollector.class)
                    .withPropertyValues(
                            "naver.api.base-url=https://naverapihub.apigw.ntruss.com",
                            "naver.api.client-id=" + System.getenv("NAVER_CLIENT_ID"),
                            "naver.api.client-secret=" + System.getenv("NAVER_CLIENT_SECRET")
                    );

    @Test
    void collect() {
        contextRunner.run(context -> {

            NaverNewsCollector collector =
                    context.getBean(NaverNewsCollector.class);

            List<CollectedArticleDTO> articles = collector.collect("인공지능");

            articles.forEach(System.out::println);
        });
    }
}