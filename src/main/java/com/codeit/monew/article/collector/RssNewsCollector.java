package com.codeit.monew.article.collector;

import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RssNewsCollector implements NewsCollector {
    @Override
    public List<CollectedArticleDTO> collect(String keyword) {
        return List.of();
    }
}
