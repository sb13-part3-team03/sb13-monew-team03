package com.codeit.monew.article.repository;

import com.codeit.monew.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleRepositoryCustom {

    boolean existsBySourceUrl(String sourceUrl);

}
