package com.codeit.monew.article.service;

import com.codeit.monew.article.dto.command.ArticleSearchCommand;
import com.codeit.monew.article.dto.response.CursorPageResponseArticleDto;

import java.util.UUID;

public interface ArticleQueryService {

    CursorPageResponseArticleDto searchArticles(ArticleSearchCommand command);

}
