package com.codeit.monew.article.service;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.article.exception.ArticleNotFoundException;
import com.codeit.monew.article.repository.ArticleInterestRepository;
import com.codeit.monew.article.repository.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ArticleDeleteServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleInterestRepository articleInterestRepository;

    private ArticleDeleteService articleDeleteService;

    @BeforeEach
    void setUp() {
        articleDeleteService = new ArticleDeleteService(
                articleRepository,
                articleInterestRepository
        );
    }

    @Test
    @DisplayName("논리 삭제 테스트")
    void softDelete() {
        // given
        UUID articleId = UUID.randomUUID();

        Article article = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article/1",
                "테스트 기사",
                "테스트 요약",
                Instant.now()
        );

        given(articleRepository.findById(articleId))
                .willReturn(Optional.of(article));

        // when
        articleDeleteService.softDelete(articleId);

        // then
        assertThat(article.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("물리 삭제 테스트")
    void hardDelete() {
        // given
        UUID articleId = UUID.randomUUID();

        Article article = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article/1",
                "테스트 기사",
                "테스트 요약",
                Instant.now()
        );

        given(articleRepository.findById(articleId))
                .willReturn(Optional.of(article));

        // when
        articleDeleteService.hardDelete(articleId);

        // then
        verify(articleInterestRepository)
                .deleteAllByArticle_Id(articleId);

        verify(articleRepository)
                .delete(article);
    }

    @Test
    @DisplayName("존재하지 않는 기사를 논리 삭제하면 예외가 발생한다.")
    void softDelete_whenArticleNotFound_throwsException() {
        // given
        UUID articleId = UUID.randomUUID();

        given(articleRepository.findById(articleId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> articleDeleteService.softDelete(articleId))
                .isInstanceOf(ArticleNotFoundException.class);
    }
}