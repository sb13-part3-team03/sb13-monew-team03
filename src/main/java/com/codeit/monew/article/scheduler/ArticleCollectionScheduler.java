package com.codeit.monew.article.scheduler;

import com.codeit.monew.article.service.ArticleCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleCollectionScheduler {

    private final ArticleCollectionService articleCollectionService;

    // 매시간 정각마다 실행
    @Scheduled(cron = "0 0 * * * *")
    public void collectArticles() {
        log.info("뉴스 기사 수집 배치 시작");

        try {
            articleCollectionService.collectAndSave();

            log.info("뉴스 기사 수집 배치 완료");

        } catch (Exception e) {
            log.error("뉴스 기사 수집 배치 실패", e);
        }
    }
}
