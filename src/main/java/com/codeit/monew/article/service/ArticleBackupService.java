package com.codeit.monew.article.service;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.exception.S3StorageException;
import com.codeit.monew.article.metric.ArticleBackupMetrics;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleBackupService {

    private final ArticleRepository articleRepository;
    private final S3StorageService s3StorageService;
    private final ObjectMapper objectMapper;
    private final ArticleBackupMetrics backupMetrics;

    // 백업되지 않은 날짜 찾아 백업
    public void backup() {
        List<LocalDate> dates = findUnbackedDates();

        log.info("백업 대상 날짜: {}", dates);

        // 백업 실패 오류 확인 로그
        dates.forEach(date -> {
            log.info("기사 백업 시작. date={}", date);
            backup(date);
            log.info("기사 백업 완료. date={}", date);
        });
    }

    // 하루치 백업
    public void backup(LocalDate date) {

        backupMetrics.getBackupTimer().record(() -> {

            ZoneId zone = ZoneId.of("Asia/Seoul");

            Instant fromInstant = date
                    .atStartOfDay(zone)
                    .toInstant();

            Instant toInstant = date
                    .plusDays(1)
                    .atStartOfDay(zone)
                    .toInstant();

            List<Article> articles =
                    articleRepository
                            .findByPublishDateGreaterThanEqualAndPublishDateLessThan(
                                    fromInstant,
                                    toInstant
                            );

            log.info(
                    "백업 대상 기사 조회 완료. date={}, articleCount={}",
                    date,
                    articles.size()
            );

            try {
                String json = objectMapper.writeValueAsString(articles);

                String key = "article-backup/"
                        + date
                        + "/articles.json";

                s3StorageService.upload(key, json);

                backupMetrics.recordSuccess();
                backupMetrics.recordArticles(articles.size());

            } catch (JsonProcessingException e) {
                backupMetrics.recordFailure();

                throw new S3StorageException(
                        ErrorCode.S3_BACKUP_FAILED
                );

            } catch (S3StorageException e) {
                backupMetrics.recordFailure();

                throw e;
            }
        });
    }

    private List<LocalDate> findUnbackedDates() {

        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);

        List<Instant> publishDates =
                articleRepository.findDistinctPublishDates();

        log.info("DB publishDates 조회 결과: {}", publishDates);
        log.info("오늘 날짜: {}", today);

        return publishDates.stream()
                .map(instant -> instant.atZone(zone).toLocalDate())
                .distinct()
                .filter(date -> {
                    boolean target = date.isBefore(today);
                    log.info("백업 날짜 후보 확인. date={}, target={}",
                            date, target);
                    return target;
                })
                .filter(date -> {

                    String key =
                            "article-backup/" + date + "/articles.json";

                    boolean exists =
                            s3StorageService.exists(key);

                    log.info(
                            "S3 백업 존재 여부. key={}, exists={}",
                            key,
                            exists
                    );

                    return !exists;
                })
                .toList();
    }

}
