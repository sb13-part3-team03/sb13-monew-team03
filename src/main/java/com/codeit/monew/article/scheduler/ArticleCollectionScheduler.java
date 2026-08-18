package com.codeit.monew.article.scheduler;

import com.codeit.monew.article.service.ArticleCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleCollectionScheduler {

    private final ArticleCollectionService articleCollectionService;

    // 매시간 정각마다 실행
    @Scheduled(cron = "0 0 * * * *")
    public void collectArticles() {
        articleCollectionService.collectAndSave();
    }
}