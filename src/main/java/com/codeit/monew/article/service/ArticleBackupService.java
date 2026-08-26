package com.codeit.monew.article.service;

import com.codeit.monew.article.dto.response.ArticleRestoreResultDto;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.exception.ArticleRestoreException;
import com.codeit.monew.article.exception.S3StorageException;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

        dates.forEach(this::backup);
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

        List<Instant> publishDates =
                articleRepository.findDistinctPublishDates();

        return publishDates.stream()
                .map(instant -> instant.atZone(zone).toLocalDate())
                .distinct()
                .filter(date -> {
                    String key = "article-backup/"
                            + date
                            + "/articles.json";

                    return !s3StorageService.exists(key);
                })
                .toList();
    }

}
