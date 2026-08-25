package com.codeit.monew.article.repository;

import com.codeit.monew.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleRepositoryCustom {

    Optional<Article> findBySourceUrl(String sourceUrl);

    Optional<Article> findByIdAndDeletedAtIsNull(UUID articleId);

    List<Article> findByPublishDateGreaterThanEqualAndPublishDateLessThan(Instant from, Instant to);

}
