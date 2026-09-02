package com.codeit.monew.article.mapper;

import com.codeit.monew.article.dto.response.ArticleViewDto;
import com.codeit.monew.article.dto.response.ArticleViewResultDto;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.article.entity.ArticleView;
import com.codeit.monew.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class ArticleViewMapperTest {

    private ArticleViewMapper articleViewMapper;

    @BeforeEach
    void setUp() {
        articleViewMapper = Mappers.getMapper(ArticleViewMapper.class);
    }

    @Test
    @DisplayName("ArticleViewResultDto를 ArticleViewDto로 변환한다")
    void toDto() {
        // given
        Instant publishDate = Instant.parse("2026-09-01T10:00:00Z");

        User user = new User("user1@test.com", "user1", "password");

        Article article = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article-1",
                "Article 1",
                "뉴스 기사입니다.",
                publishDate
        );

        ArticleView articleView = ArticleView.create(article, user);

        ArticleViewResultDto result =
                new ArticleViewResultDto(
                        articleView,
                        3L,
                        10L
                );

        // when
        ArticleViewDto dto = articleViewMapper.toDto(result);

        // then
        assertThat(dto.id()).isEqualTo(articleView.getId());
        assertThat(dto.viewedBy()).isEqualTo(user.getId());
        assertThat(dto.createdAt()).isEqualTo(articleView.getCreatedAt());
        assertThat(dto.articleId()).isEqualTo(article.getId());
        assertThat(dto.source()).isEqualTo("NAVER");
        assertThat(dto.sourceUrl()).isEqualTo("https://example.com/article-1");
        assertThat(dto.articleTitle()).isEqualTo("Article 1");
        assertThat(dto.articlePublishedDate()).isEqualTo(publishDate);
        assertThat(dto.articleSummary()).isEqualTo("뉴스 기사입니다.");
        assertThat(dto.articleCommentCount()).isEqualTo(3L);
        assertThat(dto.articleViewCount()).isEqualTo(10L);
    }

}
