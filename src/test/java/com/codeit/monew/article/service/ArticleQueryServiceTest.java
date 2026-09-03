package com.codeit.monew.article.service;

import com.codeit.monew.article.dto.command.ArticleSearchCommand;
import com.codeit.monew.article.dto.response.ArticleDto;
import com.codeit.monew.article.dto.response.ArticleSearchResultDto;
import com.codeit.monew.article.dto.response.CursorPageResponseArticleDto;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.article.exception.ArticleNotFoundException;
import com.codeit.monew.article.mapper.ArticleMapper;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.repository.ArticleViewRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleQueryServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ArticleViewRepository articleViewRepository;

    @Mock
    private ArticleMapper articleMapper;

    @InjectMocks
    private ArticleQueryService articleQueryService;

    @Test
    @DisplayName("기사 검색 - 다음 페이지가 존재하면 nextCursor와 nextAfter를 생성한다")
    void searchArticles_hasNext() {
        // given
        ArticleSearchCommand command = new ArticleSearchCommand(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "commentCount",
                "desc",
                2,
                UUID.randomUUID()
        );

        Article article1 = mock(Article.class);
        Article article2 = mock(Article.class);
        Article article3 = mock(Article.class);

        Instant createdAt2 = Instant.parse("2026-08-27T10:00:00Z");

        when(article2.getCreatedAt()).thenReturn(createdAt2);

        ArticleSearchResultDto result1 =
                new ArticleSearchResultDto(article1, 10L, 100L, false);

        ArticleSearchResultDto result2 =
                new ArticleSearchResultDto(article2, 8L, 80L, false);

        ArticleSearchResultDto result3 =
                new ArticleSearchResultDto(article3, 5L, 50L, false);

        // limit = 2이므로 3개를 반환 → hasNext = true
        when(articleRepository.searchArticles(command, command.orderBy()))
                .thenReturn(List.of(result1, result2, result3));

        when(articleRepository.countTotalElements(command)).thenReturn(10L);

        ArticleDto dto1 = mock(ArticleDto.class);
        ArticleDto dto2 = mock(ArticleDto.class);

        when(articleMapper.toDtoList(List.of(result1, result2))).thenReturn(List.of(dto1, dto2));

        // when
        CursorPageResponseArticleDto response = articleQueryService.searchArticles(command);

        // then
        assertThat(response.content()).containsExactly(dto1, dto2);

        assertThat(response.nextCursor()).isEqualTo("8");

        assertThat(response.nextAfter()).isEqualTo(createdAt2);


        assertThat(response.size()).isEqualTo(2);

        assertThat(response.totalElements()).isEqualTo(10L);

        assertThat(response.hasNext()).isTrue();

        verify(articleRepository).searchArticles(command, command.orderBy());

        verify(articleRepository).countTotalElements(command);

        verify(articleMapper).toDtoList(List.of(result1, result2));
    }

    @Test
    @DisplayName("출처 목록 조회")
    void getSources_returnsAllArticleSources() {
        List<ArticleSource> result = articleQueryService.getSources();

        assertThat(result).containsExactlyElementsOf(
                List.of(ArticleSource.values())
        );
    }

    @Test
    @DisplayName("기사 ID로 삭제되지 않은 기사를 조회한다")
    void getArticle_success() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Article article = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article",
                "테스트 기사",
                "테스트 요약",
                Instant.now()
        );

        ReflectionTestUtils.setField(article, "id", articleId);

        given(articleRepository.findByIdAndDeletedAtIsNull(articleId))
                .willReturn(Optional.of(article));

        given(commentRepository.countByArticleIdAndDeletedAtIsNull(articleId))
                .willReturn(3L);

        given(articleViewRepository.countByArticleId(articleId))
                .willReturn(10L);

        given(articleViewRepository.existsByArticle_IdAndUser_Id(articleId, userId))
                .willReturn(true);

        ArticleDto expected = new ArticleDto(
                articleId,
                article.getSource(),
                article.getSourceUrl(),
                article.getTitle(),
                article.getPublishDate(),
                article.getSummary(),
                3,
                10,
                true
        );

        given(articleMapper.toDto(
                article,
                3L,
                10L,
                true
        )).willReturn(expected);

        // when
        ArticleDto result = articleQueryService.getArticle(articleId, userId);

        // then
        assertThat(result)
                .isEqualTo(expected);

        verify(articleRepository)
                .findByIdAndDeletedAtIsNull(articleId);

        verify(commentRepository)
                .countByArticleIdAndDeletedAtIsNull(articleId);

        verify(articleViewRepository)
                .countByArticleId(articleId);

        verify(articleViewRepository)
                .existsByArticle_IdAndUser_Id(articleId, userId);

        verify(articleMapper)
                .toDto(article, 3L, 10L, true);
    }

    @Test
    @DisplayName("존재하지 않는 기사 조회 시 ArticleNotFoundException이 발생한다")
    void getArticle_notFound() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(articleRepository.findByIdAndDeletedAtIsNull(articleId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                articleQueryService.getArticle(articleId, userId)
        )
                .isInstanceOf(ArticleNotFoundException.class);

        verify(articleRepository)
                .findByIdAndDeletedAtIsNull(articleId);

        verify(commentRepository, never())
                .countByArticleIdAndDeletedAtIsNull(any());

        verify(articleViewRepository, never())
                .countByArticleId(any());

        verify(articleViewRepository, never())
                .existsByArticle_IdAndUser_Id(any(), any());

        verify(articleMapper, never())
                .toDto(any(), anyLong(), anyLong(), anyBoolean());
    }

}
