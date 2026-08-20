package com.codeit.monew.article.repository;

import com.codeit.monew.article.entity.ArticleView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ArticleViewRepository extends JpaRepository<UUID, ArticleView> {

    ArticleView save(ArticleView articleView);

    long countByArticleId(UUID articleId);

}