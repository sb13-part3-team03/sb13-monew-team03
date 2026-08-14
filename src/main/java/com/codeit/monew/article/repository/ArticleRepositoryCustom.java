package com.codeit.monew.article.repository;

import com.codeit.monew.article.dto.request.ArticleSearchRequest;
import com.codeit.monew.article.dto.response.CursorPageResponseArticleDto;
import com.codeit.monew.article.entity.Article;

import java.util.List;

public interface ArticleRepositoryCustom {

    List<Article> searchArticles(ArticleSearchRequest request);

}