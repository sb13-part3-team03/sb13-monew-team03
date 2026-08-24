package com.codeit.monew.article.service;

import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleInterest;
import com.codeit.monew.article.entity.ArticleInterestId;
import com.codeit.monew.article.repository.ArticleInterestRepository;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.interest.entity.Interest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleSaveService {

    private final ArticleRepository articleRepository;
    private final ArticleInterestRepository articleInterestRepository;

    @Transactional
    public void saveOneArticle(
            CollectedArticleDTO dto,
            Interest interest
    ) {

        // 1. 기존 기사 조회 / 없으면 저장
        Article article = articleRepository
                .findBySourceUrl(dto.sourceUrl())
                .orElseGet(() -> saveArticle(dto));

        // 2. 기사-관심사 연결
        boolean newlyLinked =
                saveArticleInterest(article, interest);

        // 이미 같은 기사-관심사가 연결돼 있으면
        // 중복 알림도 생성하지 않음
        if (!newlyLinked) {
            return;
        }

        // 3. 알림 생성
        // 알림 코드 추가 예정
    }

    private Article saveArticle(
            CollectedArticleDTO dto
    ) {

        Article article = Article.create(
                dto.source(),
                dto.sourceUrl(),
                dto.title(),
                dto.summary(),
                dto.publishDate()
        );

        return articleRepository.save(article);
    }

    private boolean saveArticleInterest(
            Article article,
            Interest interest
    ) {

        ArticleInterestId id = new ArticleInterestId(
                article.getId(),
                interest.getId()
        );

        if (articleInterestRepository.existsById(id)) {
            return false;
        }

        ArticleInterest articleInterest =
                ArticleInterest.create(
                        article,
                        interest
                );

        articleInterestRepository.save(articleInterest);

        return true;
    }
}