package com.codeit.monew.article.repository;

import com.codeit.monew.article.dto.command.ArticleSearchCommand;
import com.codeit.monew.article.dto.response.ArticleSearchResult;

import java.util.List;

public interface ArticleRepositoryCustom {

    long countTotalElements(ArticleSearchCommand command);

    List<ArticleSearchResult> searchArticles(ArticleSearchCommand command, String orderBy);

}