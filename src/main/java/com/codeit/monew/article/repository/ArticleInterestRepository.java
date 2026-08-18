package com.codeit.monew.article.repository;

import com.codeit.monew.article.entity.ArticleInterest;
import com.codeit.monew.article.entity.ArticleInterestId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleInterestRepository
        extends JpaRepository<ArticleInterest, ArticleInterestId> {
}