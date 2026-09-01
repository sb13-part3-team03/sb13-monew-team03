package com.codeit.monew.article.service;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.exception.S3StorageException;
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

        try {
            String json = objectMapper.writeValueAsString(articles);

            String key = "article-backup/"
                    + date
                    + "/articles.json";

            s3StorageService.upload(key, json);

        } catch (JsonProcessingException e) {
            throw new S3StorageException(ErrorCode.S3_BACKUP_FAILED);
        }
    }

    private List<LocalDate> findUnbackedDates() {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        // 당일 발행 기사는 이후에도 추가될 수 있으므로 백업 대상에서 제외
        LocalDate today = LocalDate.now(zone);

        List<Instant> publishDates =
                articleRepository.findDistinctPublishDates();

        return publishDates.stream()
                .map(instant -> instant.atZone(zone).toLocalDate())
                .distinct()
                .filter(date -> date.isBefore(today))
                .filter(date -> {
                    String key = "article-backup/"
                            + date
                            + "/articles.json";

                    return !s3StorageService.exists(key);
                })
                .toList();

    }

}
