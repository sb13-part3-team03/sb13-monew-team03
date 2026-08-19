package com.codeit.monew.article.collector;

import com.codeit.monew.article.dto.collection.CollectedArticleDTO;

import java.util.List;

public interface NewsCollector {

    List<CollectedArticleDTO> collect(String keyword);
}