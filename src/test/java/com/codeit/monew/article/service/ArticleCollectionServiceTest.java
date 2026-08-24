package com.codeit.monew.article.service;

import com.codeit.monew.article.collector.NewsCollector;
import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import com.codeit.monew.article.entity.ArticleSource;
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ArticleCollectionServiceTest {

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private NewsCollector newsCollector;

    @Mock
    private ArticleSaveService articleSaveService;

    private ArticleCollectionService articleCollectionService;

    @BeforeEach
    void setUp() {
        articleCollectionService = new ArticleCollectionService(
                interestRepository,
                List.of(newsCollector),
                articleSaveService
        );
    }

    @Test
    @DisplayName("수집한 기사를 기사 저장 서비스에 전달한다.")
    void collectAndSave_whenArticleCollected_callsArticleSaveService() {

        Interest interest = new Interest(
                "인공지능",
                List.of("AI")
        );

        CollectedArticleDTO collectedArticle =
                createCollectedArticle(
                        "https://example.com/article/1",
                        "AI 관련 뉴스"
                );

        given(interestRepository.findAllWithKeywords())
                .willReturn(List.of(interest));

        given(newsCollector.collect("AI"))
                .willReturn(List.of(collectedArticle));

        articleCollectionService.collectAndSave();

        verify(articleSaveService)
                .saveOneArticle(collectedArticle, interest);
    }

    @Test
    @DisplayName("등록된 관심사가 없으면 뉴스 수집기를 호출하지 않는다.")
    void collectAndSave_whenNoInterests_doesNotCollectNews() {

        given(interestRepository.findAllWithKeywords())
                .willReturn(List.of());

        articleCollectionService.collectAndSave();

        verify(newsCollector, never())
                .collect(any());

        verify(articleSaveService, never())
                .saveOneArticle(any(), any());
    }

    @Test
    @DisplayName("뉴스 수집 중 예외가 발생해도 전체 수집은 중단되지 않는다.")
    void collectAndSave_whenCollectorThrowsException_doesNotThrow() {

        Interest interest = new Interest(
                "인공지능",
                List.of("AI")
        );

        given(interestRepository.findAllWithKeywords())
                .willReturn(List.of(interest));

        given(newsCollector.collect("AI"))
                .willThrow(new IllegalStateException("수집 실패"));

        assertThatCode(
                () -> articleCollectionService.collectAndSave()
        ).doesNotThrowAnyException();

        verify(articleSaveService, never())
                .saveOneArticle(any(), any());
    }

    @Test
    @DisplayName("한 기사 저장에 실패해도 다음 기사는 계속 처리한다.")
    void collectAndSave_whenOneArticleFails_continuesNextArticle() {

        Interest interest = new Interest(
                "인공지능",
                List.of("AI")
        );

        CollectedArticleDTO first =
                createCollectedArticle(
                        "https://example.com/article/1",
                        "기사1"
                );

        CollectedArticleDTO second =
                createCollectedArticle(
                        "https://example.com/article/2",
                        "기사2"
                );

        given(interestRepository.findAllWithKeywords())
                .willReturn(List.of(interest));

        given(newsCollector.collect("AI"))
                .willReturn(List.of(first, second));

        willThrow(
                new IllegalStateException("저장 실패")
        ).given(articleSaveService)
                .saveOneArticle(first, interest);

        articleCollectionService.collectAndSave();

        verify(articleSaveService)
                .saveOneArticle(first, interest);

        verify(articleSaveService)
                .saveOneArticle(second, interest);
    }

    private CollectedArticleDTO createCollectedArticle(
            String sourceUrl,
            String title
    ) {
        return new CollectedArticleDTO(
                ArticleSource.NAVER,
                sourceUrl,
                title,
                title + " 요약",
                Instant.parse("2026-08-18T03:00:00Z")
        );
    }
}