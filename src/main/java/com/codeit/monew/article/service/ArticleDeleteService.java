package com.codeit.monew.article.service;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.exception.ArticleNotFoundException;
import com.codeit.monew.article.repository.ArticleInterestRepository;
import com.codeit.monew.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleDeleteService {

    private final ArticleRepository articleRepository;
    private final ArticleInterestRepository articleInterestRepository;

    @Transactional
    public void softDelete(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(ArticleNotFoundException::new);

        article.delete();
    }

    @Transactional
    public void hardDelete(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(ArticleNotFoundException::new);

        articleInterestRepository.deleteAllByArticle_Id(articleId);
        articleRepository.delete(article);
    }
}