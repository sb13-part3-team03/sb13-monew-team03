package com.codeit.monew.article.service;

import com.codeit.monew.article.collector.NewsCollector;
import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
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
    private final List<NewsCollector> newsCollectors;
    private final ArticleSaveService articleSaveService;

    public void collectAndSave() {

        // 1. 등록된 모든 관심사 조회
        List<Interest> interests =
                interestRepository.findAllWithKeywords();

        // 2. 관심사별 처리
        for (Interest interest : interests) {

            // 3. 해당 관심사의 키워드별 처리
            for (String keyword : interest.getKeywords()) {

                // 4. 등록된 뉴스 수집기 실행
                for (NewsCollector collector : newsCollectors) {

                    List<CollectedArticleDTO> collectedArticles;

                    try {
                        // 외부 API/RSS 수집은 트랜잭션 밖
                        collectedArticles =
                                collector.collect(keyword);

                    } catch (Exception e) {
                        log.error(
                                "뉴스 수집 실패. collector={}, keyword={}",
                                collector.getClass().getSimpleName(),
                                keyword,
                                e
                        );
                        continue;
                    }

                    // 5. 수집된 기사 한 건씩 저장
                    for (CollectedArticleDTO dto : collectedArticles) {

                        try {
                            articleSaveService.saveOneArticle(
                                    dto,
                                    interest
                            );

                        } catch (Exception e) {
                            log.error(
                                    "기사 저장 실패. sourceUrl={}",
                                    dto.sourceUrl(),
                                    e
                            );
                        }
                    }
                }
            }
        }
    }
}