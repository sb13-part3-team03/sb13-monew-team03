package com.codeit.monew.article.service;

import com.codeit.monew.article.dto.request.ArticleSearchRequest;
import com.codeit.monew.article.dto.response.CursorPageResponseArticleDto;
import com.codeit.monew.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
public class ArticleQueryServiceImpl implements ArticleQueryService {

    private final ArticleRepository articleRepository;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseArticleDto searchArticles(ArticleSearchRequest request, UUID userId) {
        return null;
    }
}
