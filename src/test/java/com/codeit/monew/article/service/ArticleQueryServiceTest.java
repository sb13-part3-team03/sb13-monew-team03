package com.codeit.monew.article.service;

import com.codeit.monew.article.dto.command.ArticleSearchCommand;
import com.codeit.monew.article.dto.response.ArticleDto;
import com.codeit.monew.article.dto.response.ArticleSearchResult;
import com.codeit.monew.article.dto.response.CursorPageResponseArticleDto;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.mapper.ArticleMapper;
import com.codeit.monew.article.repository.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleQueryServiceTest {

    @Mock
    private ArticleRepository articleRepository;

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
                "commentCount",
                "desc",
                2,
                UUID.randomUUID()
        );

        Article article1 = mock(Article.class);
        Article article2 = mock(Article.class);
        Article article3 = mock(Article.class);

        UUID id2 = UUID.randomUUID();

        when(article2.getId()).thenReturn(id2);

        ArticleSearchResult result1 =
                new ArticleSearchResult(article1, 10L, 100L, false);

        ArticleSearchResult result2 =
                new ArticleSearchResult(article2, 8L, 80L, false);

        ArticleSearchResult result3 =
                new ArticleSearchResult(article3, 5L, 50L, false);

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

        assertThat(response.nextAfter()).isEqualTo(id2);

        assertThat(response.size()).isEqualTo(2);

        assertThat(response.totalElements()).isEqualTo(10L);

        assertThat(response.hasNext()).isTrue();

        verify(articleRepository).searchArticles(command, command.orderBy());

        verify(articleRepository).countTotalElements(command);

        verify(articleMapper).toDtoList(List.of(result1, result2));
    }

}
