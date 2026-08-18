package com.codeit.monew.article.service;

import com.codeit.monew.article.entity.Article;
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
                .orElseThrow(() -> new IllegalArgumentException("기사를 찾을 수 없습니다."));

        article.delete();
    }

    @Transactional
    public void hardDelete(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("기사를 찾을 수 없습니다."));

        articleInterestRepository.deleteAllByArticle_Id(articleId);
        articleRepository.delete(article);
    }
}