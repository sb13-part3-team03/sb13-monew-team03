package com.codeit.monew.article.repository;

import com.codeit.monew.article.entity.ArticleView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleViewRepository extends JpaRepository<UUID, ArticleView> {

    ArticleView save(ArticleView articleView);

    // 기록 존재 여부 확인
    Optional<ArticleView> findByArticleIdAndUserId(
            UUID articleId,
            UUID userId
    );

    long countByArticleId(UUID articleId);

    void deleteAllByArticle_Id(UUID articleId);

}