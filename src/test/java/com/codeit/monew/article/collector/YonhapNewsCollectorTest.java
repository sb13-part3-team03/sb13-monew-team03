package com.codeit.monew.article.collector;

import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class YonhapNewsCollectorTest {

    private YonhapNewsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new YonhapNewsCollector();
    }

    @Test
    void collect() {
        List<CollectedArticleDTO> articles =
                collector.collect("AI");

        articles.forEach(System.out::println);
    }
}
