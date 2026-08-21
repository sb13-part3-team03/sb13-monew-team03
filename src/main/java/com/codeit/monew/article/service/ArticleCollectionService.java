package com.codeit.monew.article.service;

import com.codeit.monew.article.collector.NewsCollector;
import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleInterest;
import com.codeit.monew.article.entity.ArticleInterestId;
import com.codeit.monew.article.repository.ArticleInterestRepository;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleCollectionService {

    private final InterestRepository interestRepository;
    private final ArticleRepository articleRepository;
    private final ArticleInterestRepository articleInterestRepository;
    private final List<NewsCollector> newsCollectors;

    public void collectAndSave() {

        // 1. 등록된 모든 관심사 조회
        List<Interest> interests = interestRepository.findAllWithKeywords();

        // 2. 관심사별로 처리
        for (Interest interest : interests) {

            // 3. 해당 관심사의 키워드별로 처리
            for (String keyword : interest.getKeywords()) {

                // 4. 등록된 뉴스 수집기 실행
                for (NewsCollector collector : newsCollectors) {

                    try {
                        List<CollectedArticleDTO> collectedArticles =
                                collector.collect(keyword);

                        // 5. 수집된 기사 저장
                        for (CollectedArticleDTO dto : collectedArticles) {

                            Article article = articleRepository
                                    .findBySourceUrl(dto.sourceUrl())
                                    .orElseGet(() -> saveArticle(dto));

                            // 6. 기사와 관심사 연결
                            saveArticleInterest(article, interest);
                        }

                    } catch (Exception e) {
                        log.error(
                                "뉴스 수집 또는 저장 실패. collector={}, keyword={}",
                                collector.getClass().getSimpleName(),
                                keyword,
                                e
                        );
                    }
                }
            }
        }
    }

    private Article saveArticle(CollectedArticleDTO dto) {

        Article article = Article.create(
                dto.source(),
                dto.sourceUrl(),
                dto.title(),
                dto.summary(),
                dto.publishDate()
        );

        return articleRepository.save(article);
    }

    private void saveArticleInterest(
            Article article,
            Interest interest
    ) {

        ArticleInterestId id = new ArticleInterestId(
                article.getId(),
                interest.getId()
        );

        // 이미 해당 기사와 관심사가 연결되어 있으면 중복 저장하지 않음
        if (articleInterestRepository.existsById(id)) {
            return;
        }

        ArticleInterest articleInterest =
                ArticleInterest.create(article, interest);

        articleInterestRepository.save(articleInterest);
    }
}