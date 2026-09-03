package com.codeit.monew.article.service;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.exception.ArticleNotFoundException;
import com.codeit.monew.article.repository.ArticleInterestRepository;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.repository.ArticleViewRepository;
import com.codeit.monew.comment.repository.CommentLikeRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleDeleteService {

    private final ArticleRepository articleRepository;
    private final ArticleInterestRepository articleInterestRepository;
    private final ArticleViewRepository articleViewRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    // 논리 삭제
    @Transactional
    public void softDelete(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(ArticleNotFoundException::new);

        article.delete();
    }

    // 물리 삭제
    @Transactional
    public void hardDelete(UUID articleId) {

        Article article = articleRepository.findById(articleId)
                .orElseThrow(ArticleNotFoundException::new);

        commentLikeRepository.deleteAllByComment_Article_Id(articleId);
        commentRepository.deleteAllByArticle_Id(articleId);
        articleInterestRepository.deleteAllByArticle_Id(articleId);
        articleViewRepository.deleteAllByArticle_Id(articleId);

        articleRepository.delete(article);

    }
}