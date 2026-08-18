package com.codeit.monew.article.collector;

import com.codeit.monew.article.collector.ChosunNewsCollector;
import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class ChosunNewsCollectorTest {

    private ChosunNewsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new ChosunNewsCollector();
    }

    @Test
    void collect() {
        List<CollectedArticleDTO> articles =
                collector.collect("AI");

        articles.forEach(System.out::println);
    }
}