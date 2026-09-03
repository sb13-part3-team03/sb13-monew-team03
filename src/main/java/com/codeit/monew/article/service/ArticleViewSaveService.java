package com.codeit.monew.article.service;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleView;
import com.codeit.monew.article.repository.ArticleViewRepository;
import com.codeit.monew.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleViewSaveService {

    private final ArticleViewRepository articleViewRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ArticleView save(Article article, User user) {
        try {
            return articleViewRepository.saveAndFlush(
                    ArticleView.create(article, user)
            );
        } catch (DataIntegrityViolationException e) {
            return articleViewRepository
                    .findByArticleIdAndUserId(
                            article.getId(),
                            user.getId()
                    )
                    .orElseThrow(() -> e);
        }
    }
}
