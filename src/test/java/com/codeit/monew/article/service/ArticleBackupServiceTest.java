package com.codeit.monew.article.service;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.article.exception.S3StorageException;
import com.codeit.monew.article.metric.ArticleBackupMetrics;
import com.codeit.monew.article.repository.ArticleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArticleBackupServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private S3StorageService s3StorageService;

    @Mock
    private ObjectMapper objectMapper;

    private SimpleMeterRegistry meterRegistry;
    private ArticleBackupMetrics backupMetrics;
    private ArticleBackupService articleBackupService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        backupMetrics = new ArticleBackupMetrics(meterRegistry);

        articleBackupService = new ArticleBackupService(
                articleRepository,
                s3StorageService,
                objectMapper,
                backupMetrics
        );
    }

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

    @Test
    @DisplayName("기사 백업 성공 시 성공 횟수, 기사 수, 실행 시간을 기록한다")
    void backup_success_recordsMetrics() throws Exception {

        // given
        LocalDate date = LocalDate.of(2026, 8, 25);
        ZoneId zone = ZoneId.of("Asia/Seoul");

        Instant fromInstant = date
                .atStartOfDay(zone)
                .toInstant();

        Instant toInstant = date
                .plusDays(1)
                .atStartOfDay(zone)
                .toInstant();

        Article article1 = mock(Article.class);
        Article article2 = mock(Article.class);

        List<Article> articles =
                List.of(article1, article2);

        when(articleRepository
                .findByPublishDateGreaterThanEqualAndPublishDateLessThan(
                        fromInstant,
                        toInstant
                ))
                .thenReturn(articles);

        when(objectMapper.writeValueAsString(articles))
                .thenReturn("[{},{}]");

        // when
        articleBackupService.backup(date);

        // then
        verify(s3StorageService).upload(
                "article-backup/2026-08-25/articles.json",
                "[{},{}]"
        );

        assertThat(
                meterRegistry
                        .counter("article.backup.success")
                        .count()
        ).isEqualTo(1.0);

        assertThat(
                meterRegistry
                        .counter("article.backup.failure")
                        .count()
        ).isEqualTo(0.0);

        assertThat(
                meterRegistry
                        .counter("article.backup.articles")
                        .count()
        ).isEqualTo(2.0);

        assertThat(
                meterRegistry
                        .timer("article.backup.duration")
                        .count()
        ).isEqualTo(1L);
    }

    @Test
    @DisplayName("기사 백업 실패 시 실패 횟수를 기록한다")
    void backup_failure_recordsMetrics() throws Exception {

        // given
        LocalDate date = LocalDate.of(2026, 8, 25);

        when(articleRepository
                .findByPublishDateGreaterThanEqualAndPublishDateLessThan(
                        any(Instant.class),
                        any(Instant.class)
                ))
                .thenReturn(List.of(mock(Article.class)));

        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("JSON 변환 실패") {
                });

        // when & then
        assertThatThrownBy(() ->
                articleBackupService.backup(date)
        ).isInstanceOf(S3StorageException.class);

        assertThat(
                meterRegistry
                        .counter("article.backup.success")
                        .count()
        ).isEqualTo(0.0);

        assertThat(
                meterRegistry
                        .counter("article.backup.failure")
                        .count()
        ).isEqualTo(1.0);
    }

}
