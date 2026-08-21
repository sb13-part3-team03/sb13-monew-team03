package com.codeit.monew.article.service;

import com.codeit.monew.article.collector.NewsCollector;
import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleInterest;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.article.repository.ArticleInterestRepository;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.repository.InterestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ArticleCollectionServiceTest {

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleInterestRepository articleInterestRepository;

    @Mock
    private NewsCollector newsCollector;

    private ArticleCollectionService articleCollectionService;

    @BeforeEach
    void setUp() {
        articleCollectionService = new ArticleCollectionService(
                interestRepository,
                articleRepository,
                articleInterestRepository,
                List.of(newsCollector)
        );
    }

    @Test
    @DisplayName("새로운 기사를 수집하면 기사를 저장한다.")
    void collectAndSave_whenNewArticle_savesArticle() {
        // given
        Interest interest = new Interest(
                "인공지능",
                List.of("AI")
        );

        CollectedArticleDTO collectedArticle = new CollectedArticleDTO(
                ArticleSource.NAVER,
                "https://example.com/article/1",
                "AI 관련 뉴스",
                "AI 관련 뉴스 요약",
                Instant.parse("2026-08-18T03:00:00Z")
        );

        given(interestRepository.findAll())
                .willReturn(List.of(interest));

        given(newsCollector.collect("AI"))
                .willReturn(List.of(collectedArticle));

        given(articleRepository.findBySourceUrl(
                "https://example.com/article/1"
        )).willReturn(Optional.empty());

        given(articleRepository.save(any(Article.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        articleCollectionService.collectAndSave();

        // then
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    @DisplayName("이미 존재하는 기사라면 중복 저장하지 않는다.")
    void collectAndSave_whenArticleAlreadyExists_doesNotSaveArticle() {
        // given
        Interest interest = new Interest(
                "인공지능",
                List.of("AI")
        );

        CollectedArticleDTO collectedArticle = new CollectedArticleDTO(
                ArticleSource.NAVER,
                "https://example.com/article/1",
                "AI 관련 뉴스",
                "AI 관련 뉴스 요약",
                Instant.parse("2026-08-18T03:00:00Z")
        );

        Article existingArticle = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article/1",
                "AI 관련 뉴스",
                "AI 관련 뉴스 요약",
                Instant.parse("2026-08-18T03:00:00Z")
        );

        given(interestRepository.findAll())
                .willReturn(List.of(interest));

        given(newsCollector.collect("AI"))
                .willReturn(List.of(collectedArticle));

        given(articleRepository.findBySourceUrl(
                "https://example.com/article/1"
        )).willReturn(Optional.of(existingArticle));

        // when
        articleCollectionService.collectAndSave();

        // then
        verify(articleRepository, never())
                .save(any(Article.class));
    }

    @Test
    @DisplayName("새로운 기사를 저장하고 관심사와 연결한다.")
    void collectAndSave_whenNewArticle_savesArticleInterest() {
        // given
        Interest interest = new Interest(
                "인공지능",
                List.of("AI")
        );

        CollectedArticleDTO collectedArticle = new CollectedArticleDTO(
                ArticleSource.NAVER,
                "https://example.com/article/1",
                "AI 관련 뉴스",
                "AI 관련 뉴스 요약",
                Instant.parse("2026-08-18T03:00:00Z")
        );

        given(interestRepository.findAll())
                .willReturn(List.of(interest));

        given(newsCollector.collect("AI"))
                .willReturn(List.of(collectedArticle));

        given(articleRepository.findBySourceUrl(
                "https://example.com/article/1"
        )).willReturn(Optional.empty());

        given(articleRepository.save(any(Article.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        given(articleInterestRepository.existsById(any()))
                .willReturn(false);

        // when
        articleCollectionService.collectAndSave();

        // then
        verify(articleRepository)
                .save(any(Article.class));

        verify(articleInterestRepository)
                .save(any(ArticleInterest.class));
    }

    @Test
    @DisplayName("기사와 관심사가 이미 연결되어 있으면 중복 저장하지 않는다.")
    void collectAndSave_whenArticleInterestAlreadyExists_doesNotSaveArticleInterest() {
        // given
        Interest interest = new Interest(
                "인공지능",
                List.of("AI")
        );

        CollectedArticleDTO collectedArticle = new CollectedArticleDTO(
                ArticleSource.NAVER,
                "https://example.com/article/1",
                "AI 관련 뉴스",
                "AI 관련 뉴스 요약",
                Instant.parse("2026-08-18T03:00:00Z")
        );

        Article existingArticle = Article.create(
                collectedArticle.source(),
                collectedArticle.sourceUrl(),
                collectedArticle.title(),
                collectedArticle.summary(),
                collectedArticle.publishDate()
        );

        given(interestRepository.findAll())
                .willReturn(List.of(interest));

        given(newsCollector.collect("AI"))
                .willReturn(List.of(collectedArticle));

        given(articleRepository.findBySourceUrl(
                "https://example.com/article/1"
        )).willReturn(Optional.of(existingArticle));

        given(articleInterestRepository.existsById(any()))
                .willReturn(true);

        // when
        articleCollectionService.collectAndSave();

        // then
        verify(articleInterestRepository, never())
                .save(any(ArticleInterest.class));
    }

    @Test
    @DisplayName("등록된 관심사가 없으면 뉴스 수집기를 호출하지 않는다.")
    void collectAndSave_whenNoInterests_doesNotCollectNews() {

        // given
        given(interestRepository.findAll())
                .willReturn(List.of());

        // when
        articleCollectionService.collectAndSave();

        // then
        verify(newsCollector, never()).collect(any());
    }

    @Test
    @DisplayName("뉴스 수집 중 예외가 발생해도 전체 수집은 중단되지 않는다.")
    void collectAndSave_whenCollectorThrowsException_doesNotThrow() {

        // given
        Interest interest = new Interest(
                "인공지능",
                List.of("AI")
        );

        given(interestRepository.findAll())
                .willReturn(List.of(interest));

        given(newsCollector.collect("AI"))
                .willThrow(new IllegalStateException("수집 실패"));

        // when & then
        assertThatCode(() -> articleCollectionService.collectAndSave())
                .doesNotThrowAnyException();

        verify(articleRepository, never())
                .save(any(Article.class));
    }
}