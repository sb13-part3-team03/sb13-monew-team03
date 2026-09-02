package com.codeit.monew.article.mapper;

import com.codeit.monew.article.dto.response.ArticleDto;
import com.codeit.monew.article.dto.response.ArticleSearchResultDto;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ArticleMapperTest {

    private ArticleMapper articleMapper;

    @BeforeEach
    void setUp() {
        articleMapper = Mappers.getMapper(ArticleMapper.class);
    }

    @Test
    @DisplayName("Article을 ArticleDto로 변환한다")
    void toDto() {
        // given
        Article article = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article-1",
                "Article 1",
                "뉴스 기사입니다.",
                Instant.now()
        );

        // when
        ArticleDto result =
                articleMapper.toDto(
                        article,
                        3L,
                        10L,
                        true
                );

        // then
        assertThat(result.id()).isEqualTo(article.getId());
        assertThat(result.title()).isEqualTo(article.getTitle());
        assertThat(result.commentCount()).isEqualTo(3L);
        assertThat(result.viewCount()).isEqualTo(10L);
        assertThat(result.viewedByMe()).isTrue();
    }

    @Test
    @DisplayName("ArticleSearchResultDto 목록을 ArticleDto 목록으로 변환한다")
    void toDtoList() {
        // given
        Article article1 = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article-1",
                "Article 1",
                "뉴스 기사 1",
                Instant.parse("2026-09-01T10:00:00Z")
        );

        Article article2 = Article.create(
                ArticleSource.CHOSUN,
                "https://example.com/article-2",
                "Article 2",
                "뉴스 기사 2",
                Instant.parse("2026-09-01T11:00:00Z")
        );

        List<ArticleSearchResultDto> results = List.of(
                new ArticleSearchResultDto(
                        article1,
                        3L,
                        10L,
                        true
                ),
                new ArticleSearchResultDto(
                        article2,
                        1L,
                        5L,
                        false
                )
        );

        // when
        List<ArticleDto> dtos = articleMapper.toDtoList(results);

        // then
        assertThat(dtos).hasSize(2);

        assertThat(dtos.get(0).title()).isEqualTo("Article 1");
        assertThat(dtos.get(0).commentCount()).isEqualTo(3L);
        assertThat(dtos.get(0).viewCount()).isEqualTo(10L);

        assertThat(dtos.get(1).title()).isEqualTo("Article 2");
        assertThat(dtos.get(1).commentCount()).isEqualTo(1L);
        assertThat(dtos.get(1).viewCount()).isEqualTo(5L);
    }

}
