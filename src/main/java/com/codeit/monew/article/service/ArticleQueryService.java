package com.codeit.monew.article.service;

import com.codeit.monew.article.dto.request.ArticleSearchRequest;
import com.codeit.monew.article.dto.response.CursorPageResponseArticleDto;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface ArticleQueryService {

    CursorPageResponseArticleDto searchArticles(ArticleSearchRequest request, UUID userId);

}
