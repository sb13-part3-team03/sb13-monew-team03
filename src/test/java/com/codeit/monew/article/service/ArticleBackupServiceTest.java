package com.codeit.monew.article.service;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.article.repository.ArticleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ArticleBackupServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private S3StorageService s3StorageService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ArticleBackupService articleBackupService;

    @Test
    @DisplayName("특정 날짜의 기사를 조회해 S3에 백업한다")
    void backup_success() throws Exception {
        // given
        LocalDate date = LocalDate.of(2026, 8, 25);
        ZoneId zone = ZoneId.of("Asia/Seoul");

        Article article1 = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article1",
                "테스트 기사 1",
                "테스트 요약 1",
                date.atStartOfDay(zone).toInstant()
        );

        Article article2 = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article2",
                "테스트 기사 2",
                "테스트 요약 2",
                date.atTime(12, 0).atZone(zone).toInstant()
        );

        List<Article> articles = List.of(article1, article2);

        given(articleRepository
                .findByPublishDateGreaterThanEqualAndPublishDateLessThan(
                        any(Instant.class),
                        any(Instant.class)
                ))
                .willReturn(articles);

        given(objectMapper.writeValueAsString(articles))
                .willReturn("[{\"title\":\"테스트 기사 1\"},{\"title\":\"테스트 기사 2\"}]");

        // when
        articleBackupService.backup(date);

        // then
        verify(articleRepository)
                .findByPublishDateGreaterThanEqualAndPublishDateLessThan(
                        eq(date.atStartOfDay(zone).toInstant()),
                        eq(date.plusDays(1).atStartOfDay(zone).toInstant())
                );

        verify(objectMapper)
                .writeValueAsString(articles);

        verify(s3StorageService)
                .upload(
                        "article-backup/2026-08-25/articles.json",
                        "[{\"title\":\"테스트 기사 1\"},{\"title\":\"테스트 기사 2\"}]"
                );
    }

    @Test
    @DisplayName("이미 백업된 날짜는 백업 대상에서 제외한다")
    void backup_excludesAlreadyBackedUpDate() {
        // given
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);
        LocalDate yesterday = today.minusDays(1);

        Instant yesterdayInstant = yesterday
                .atStartOfDay(zone)
                .toInstant();

        given(articleRepository.findDistinctPublishDates())
                .willReturn(List.of(yesterdayInstant));

        given(s3StorageService.exists(
                "article-backup/" + yesterday + "/articles.json"
        )).willReturn(true);

        // when
        articleBackupService.backup();

        // then
        verify(s3StorageService, never())
                .upload(anyString(), anyString());
    }

    @Test
    @DisplayName("당일 발행 기사는 백업 대상에서 제외한다")
    void backup_excludesToday() throws JsonProcessingException {
        // given
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);
        LocalDate yesterday = today.minusDays(1);

        Instant yesterdayInstant = yesterday
                .atStartOfDay(zone)
                .toInstant();

        Instant todayInstant = today
                .atStartOfDay(zone)
                .toInstant();

        given(articleRepository.findDistinctPublishDates())
                .willReturn(List.of(yesterdayInstant, todayInstant));

        given(s3StorageService.exists(anyString()))
                .willReturn(false);

        given(articleRepository
                .findByPublishDateGreaterThanEqualAndPublishDateLessThan(any(), any()))
                .willReturn(List.of());

        given(objectMapper.writeValueAsString(any()))
                .willReturn("[]");

        // when
        articleBackupService.backup();

        // then
        verify(s3StorageService)
                .upload(
                        eq("article-backup/" + yesterday + "/articles.json"),
                        eq("[]")
                );

        verify(s3StorageService, never())
                .upload(
                        eq("article-backup/" + today + "/articles.json"),
                        anyString()
                );
    }

    @Test
    @DisplayName("백업할 날짜가 없으면 S3에 업로드하지 않는다")
    void backup_whenNoDates() {
        // given
        given(articleRepository.findDistinctPublishDates())
                .willReturn(List.of());

        // when
        articleBackupService.backup();

        // then
        verify(s3StorageService, never())
                .upload(anyString(), anyString());
    }

}
