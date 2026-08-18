package com.codeit.monew.article.collector;

import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class HankyungNewsCollectorTest {

    private HankyungNewsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new HankyungNewsCollector();
    }

    @Test
    void collect() {
        List<CollectedArticleDTO> articles =
                collector.collect("AI");

        articles.forEach(System.out::println);
    }
}