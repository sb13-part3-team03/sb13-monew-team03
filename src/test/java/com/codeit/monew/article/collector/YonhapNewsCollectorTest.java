package com.codeit.monew.article.collector;

import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import com.codeit.monew.article.entity.ArticleSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YonhapNewsCollectorTest
        extends RssNewsCollectorTestSupport {

    private YonhapNewsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new YonhapNewsCollector(feedUrl);
    }

    @Test
    @DisplayName("키워드가 포함된 연합뉴스 RSS 기사를 수집한다.")
    void collect() {

        List<CollectedArticleDTO> articles =
                collector.collect("AI");

        assertThat(articles).hasSize(1);

        CollectedArticleDTO article = articles.get(0);

        assertThat(article.source())
                .isEqualTo(ArticleSource.YEONHAP);

        assertThat(article.sourceUrl())
                .isEqualTo("https://example.com/ai");

        assertThat(article.title())
                .isEqualTo("AI 기술 발전");

        assertThat(article.summary())
                .isEqualTo("AI 관련 기사입니다.");
    }
}