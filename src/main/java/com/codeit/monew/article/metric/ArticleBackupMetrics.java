package com.codeit.monew.article.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class ArticleBackupMetrics {

    private final Counter successCounter;
    private final Counter failureCounter;
    private final Counter backedUpArticleCounter;
    @Getter
    private final Timer backupTimer;

    public ArticleBackupMetrics(MeterRegistry meterRegistry) {
        this.successCounter = Counter.builder("article.backup.success")
                .description("기사 백업 성공 횟수")
                .register(meterRegistry);

        this.failureCounter = Counter.builder("article.backup.failure")
                .description("기사 백업 실패 횟수")
                .register(meterRegistry);

        this.backedUpArticleCounter = Counter.builder("article.backup.articles")
                .description("백업된 기사 수")
                .register(meterRegistry);
        this.backupTimer = Timer.builder("article.backup.duration")
                .description("기사 백업 처리 시간")
                .register(meterRegistry);
    }

    public void recordSuccess() {
        successCounter.increment();
    }

    public void recordFailure() {
        failureCounter.increment();
    }

    public void recordArticles(int count) {
        backedUpArticleCounter.increment(count);
    }

}
