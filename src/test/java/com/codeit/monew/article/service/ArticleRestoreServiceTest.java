package com.codeit.monew.article.service;

import com.codeit.monew.article.dto.response.ArticleRestoreResultDto;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.article.exception.ArticleRestoreException;
import com.codeit.monew.article.exception.S3StorageException;
import com.codeit.monew.article.repository.ArticleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArticleRestoreServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private S3StorageService s3StorageService;

    @InjectMocks
    private ArticleRestoreService articleRestoreService;


    @Test
    @DisplayName("특정 날짜의 S3 백업에서 삭제된 기사를 복구한다")
    void restore_success() {
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
                date.atTime(12, 0)
                        .atZone(zone)
                        .toInstant()
        );

        UUID article1Id = UUID.randomUUID();
        UUID article2Id = UUID.randomUUID();

        ReflectionTestUtils.setField(article1, "id", article1Id);
        ReflectionTestUtils.setField(article2, "id", article2Id);

        List<Article> backupArticles = List.of(article1, article2);

        given(s3StorageService.download(
                eq("article-backup/2026-08-25/articles.json"),
                any(TypeReference.class)
        )).willReturn(backupArticles);

        given(articleRepository.findAllById(anyList()))
                .willReturn(List.of());

        // when
        List<ArticleRestoreResultDto> results =
                articleRestoreService.restore(date, date);

        // then
        assertThat(results).hasSize(1);

        ArticleRestoreResultDto result = results.get(0);

        assertThat(result.restoredArticleIds())
                .containsExactly(article1Id, article2Id);

        assertThat(result.restoredArticleCount()).isEqualTo(2L);

        verify(articleRepository)
                .findAllById(anyList());

        verify(articleRepository, org.mockito.Mockito.times(2))
                .insertForRestore(
                        any(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        any(),
                        any()
                );
    }

    @Test
    @DisplayName("이미 DB에 존재하는 기사는 복구하지 않는다")
    void restore_excludesExistingArticles() {
        // given
        LocalDate date = LocalDate.of(2026, 8, 25);
        ZoneId zone = ZoneId.of("Asia/Seoul");

        Article existingArticle = Article.create(
                ArticleSource.NAVER,
                "https://example.com/existing",
                "이미 존재하는 기사",
                "기존 기사 요약",
                date.atStartOfDay(zone).toInstant()
        );

        Article lostArticle = Article.create(
                ArticleSource.NAVER,
                "https://example.com/lost",
                "삭제된 기사",
                "삭제된 기사 요약",
                date.atTime(12, 0)
                        .atZone(zone)
                        .toInstant()
        );

        // 테스트에서는 ID를 직접 주입
        UUID existingArticleId = UUID.randomUUID();
        UUID lostArticleId = UUID.randomUUID();

        ReflectionTestUtils.setField(
                existingArticle,
                "id",
                existingArticleId
        );

        ReflectionTestUtils.setField(
                lostArticle,
                "id",
                lostArticleId
        );

        given(s3StorageService.download(
                eq("article-backup/2026-08-25/articles.json"),
                any(TypeReference.class)
        )).willReturn(List.of(
                existingArticle,
                lostArticle
        ));

        // DB에는 existingArticle만 존재
        given(articleRepository.findAllById(anyList()))
                .willReturn(List.of(existingArticle));

        // when
        List<ArticleRestoreResultDto> results =
                articleRestoreService.restore(date, date);

        // then
        ArticleRestoreResultDto result = results.get(0);

        // existingArticle은 제외되고 lostArticle만 복구되어야 한다.
        assertThat(result.restoredArticleIds())
                .containsExactly(lostArticleId);

        assertThat(result.restoredArticleCount())
                .isEqualTo(1L);

        // lostArticle만 INSERT되어야 한다.
        verify(articleRepository)
                .insertForRestore(
                        eq(lostArticleId),
                        eq(lostArticle.getSource().name()),
                        eq(lostArticle.getSourceUrl()),
                        eq(lostArticle.getTitle()),
                        eq(lostArticle.getSummary()),
                        eq(lostArticle.getPublishDate()),
                        eq(lostArticle.getDeletedAt())
                );
    }

    @Test
    @DisplayName("DB에 존재하는 기사가 없으면 백업된 모든 기사를 복구한다")
    void restore_allArticles_whenNoneExists() {
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
                date.atTime(12, 0)
                        .atZone(zone)
                        .toInstant()
        );

        UUID article1Id = UUID.randomUUID();
        UUID article2Id = UUID.randomUUID();

        ReflectionTestUtils.setField(article1, "id", article1Id);
        ReflectionTestUtils.setField(article2, "id", article2Id);

        given(s3StorageService.download(
                eq("article-backup/2026-08-25/articles.json"),
                any(TypeReference.class)
        )).willReturn(List.of(article1, article2));

        given(articleRepository.findAllById(anyList()))
                .willReturn(List.of());

        // when
        List<ArticleRestoreResultDto> results =
                articleRestoreService.restore(date, date);

        // then
        ArticleRestoreResultDto result = results.get(0);

        assertThat(result.restoredArticleIds())
                .containsExactly(article1Id, article2Id);

        assertThat(result.restoredArticleCount()) .isEqualTo(2L);

        verify(articleRepository)
                .insertForRestore(
                        eq(article1.getId()),
                        eq(article1.getSource().name()),
                        eq(article1.getSourceUrl()),
                        eq(article1.getTitle()),
                        eq(article1.getSummary()),
                        eq(article1.getPublishDate()),
                        eq(article1.getDeletedAt())
                );

        verify(articleRepository)
                .insertForRestore(
                        eq(article2.getId()),
                        eq(article2.getSource().name()),
                        eq(article2.getSourceUrl()),
                        eq(article2.getTitle()),
                        eq(article2.getSummary()),
                        eq(article2.getPublishDate()),
                        eq(article2.getDeletedAt())
                );
    }


    @Test
    @DisplayName("백업된 모든 기사가 이미 DB에 존재하면 복구하지 않는다")
    void restore_whenAllArticlesExist() {
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
                date.atTime(12, 0)
                        .atZone(zone)
                        .toInstant()
        );

        UUID article1Id = UUID.randomUUID();
        UUID article2Id = UUID.randomUUID();

        ReflectionTestUtils.setField(article1, "id", article1Id);
        ReflectionTestUtils.setField(article2, "id", article2Id);

        given(s3StorageService.download(
                eq("article-backup/2026-08-25/articles.json"),
                any(TypeReference.class)
        )).willReturn(List.of(article1, article2));

        given(articleRepository.findAllById(anyList()))
                .willReturn(List.of(article1, article2));

        // when
        List<ArticleRestoreResultDto> results =
                articleRestoreService.restore(date, date);

        // then
        ArticleRestoreResultDto result = results.get(0);

        assertThat(result.restoredArticleIds())
                .isEmpty();

        assertThat(result.restoredArticleCount()) .isEqualTo(0L);

        verify(articleRepository, never())
                .insertForRestore(
                        any(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        any(Instant.class),
                        any()
                );
    }


    @Test
    @DisplayName("백업 데이터가 비어 있으면 아무것도 복구하지 않는다")
    void restore_whenBackupIsEmpty() {
        // given
        LocalDate date = LocalDate.of(2026, 8, 25);

        given(s3StorageService.download(
                eq("article-backup/2026-08-25/articles.json"),
                any(TypeReference.class)
        )).willReturn(List.of());

        given(articleRepository.findAllById(anyList()))
                .willReturn(List.of());

        // when
        List<ArticleRestoreResultDto> results =
                articleRestoreService.restore(date, date);

        // then
        ArticleRestoreResultDto result = results.get(0);

        assertThat(result.restoredArticleIds())
                .isEmpty();

        assertThat(result.restoredArticleCount()) .isEqualTo(0L);

        verify(articleRepository)
                .findAllById(eq(List.of()));

        verify(articleRepository, never())
                .insertForRestore(
                        any(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        any(Instant.class),
                        any()
                );
    }


    @Test
    @DisplayName("날짜 범위에 해당하는 모든 날짜의 백업을 복구한다")
    void restore_dateRange() {
        // given
        LocalDate from = LocalDate.of(2026, 8, 25);
        LocalDate to = LocalDate.of(2026, 8, 27);
        ZoneId zone = ZoneId.of("Asia/Seoul");

        Article article1 = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article1",
                "8월 25일 기사",
                "8월 25일 요약",
                from.atStartOfDay(zone).toInstant()
        );

        Article article2 = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article2",
                "8월 26일 기사",
                "8월 26일 요약",
                from.plusDays(1)
                        .atStartOfDay(zone)
                        .toInstant()
        );

        Article article3 = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article3",
                "8월 27일 기사",
                "8월 27일 요약",
                to.atStartOfDay(zone).toInstant()
        );

        given(s3StorageService.download(
                eq("article-backup/2026-08-25/articles.json"),
                any(TypeReference.class)
        )).willReturn(List.of(article1));

        given(s3StorageService.download(
                eq("article-backup/2026-08-26/articles.json"),
                any(TypeReference.class)
        )).willReturn(List.of(article2));

        given(s3StorageService.download(
                eq("article-backup/2026-08-27/articles.json"),
                any(TypeReference.class)
        )).willReturn(List.of(article3));

        given(articleRepository.findAllById(anyList()))
                .willReturn(List.of());

        // when
        List<ArticleRestoreResultDto> results =
                articleRestoreService.restore(from, to);

        // then
        assertThat(results)
                .hasSize(3);

        assertThat(results.get(0).restoredArticleIds())
                .containsExactly(article1.getId());

        assertThat(results.get(1).restoredArticleIds())
                .containsExactly(article2.getId());

        assertThat(results.get(2).restoredArticleIds())
                .containsExactly(article3.getId());

        verify(s3StorageService)
                .download(
                        eq("article-backup/2026-08-25/articles.json"),
                        any(TypeReference.class)
                );

        verify(s3StorageService)
                .download(
                        eq("article-backup/2026-08-26/articles.json"),
                        any(TypeReference.class)
                );

        verify(s3StorageService)
                .download(
                        eq("article-backup/2026-08-27/articles.json"),
                        any(TypeReference.class)
                );

        verify(articleRepository, times(3))
                .findAllById(anyList());

        verify(articleRepository, times(3))
                .insertForRestore(
                        any(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        any(),
                        any()
                );
    }


    @Test
    @DisplayName("S3StorageException이 발생하면 해당 예외를 그대로 전달한다")
    void restore_whenS3StorageException() {
        // given
        LocalDate date = LocalDate.of(2026, 8, 25);

        S3StorageException exception = mock(S3StorageException.class);

        given(s3StorageService.download(
                eq("article-backup/2026-08-25/articles.json"),
                any(TypeReference.class)
        )).willThrow(exception);

        // when & then
        assertThatThrownBy(() ->
                articleRestoreService.restore(date, date)
        )
                .isSameAs(exception);

        verify(articleRepository, never())
                .findAllById(anyList());

        verify(articleRepository, never())
                .insertForRestore(
                        any(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        any(Instant.class),
                        any()
                );
    }


    @Test
    @DisplayName("예상하지 못한 예외가 발생하면 ArticleRestoreException으로 변환한다")
    void restore_whenUnexpectedException() {
        // given
        LocalDate date = LocalDate.of(2026, 8, 25);

        given(s3StorageService.download(
                eq("article-backup/2026-08-25/articles.json"),
                any(TypeReference.class)
        )).willThrow(
                new RuntimeException("S3 다운로드 실패")
        );

        // when & then
        assertThatThrownBy(() ->
                articleRestoreService.restore(date, date)
        )
                .isInstanceOf(ArticleRestoreException.class);
    }
}
