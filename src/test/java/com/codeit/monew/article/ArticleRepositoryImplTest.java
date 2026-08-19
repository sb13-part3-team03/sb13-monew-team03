package com.codeit.monew.article;


import com.codeit.monew.article.dto.command.ArticleSearchCommand;
import com.codeit.monew.article.dto.response.ArticleSearchResult;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.article.entity.ArticleView;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.global.config.QuerydslConfig;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.repository.InterestRepository;
import com.codeit.monew.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EnableJpaAuditing
@Import(QuerydslConfig.class)
public class ArticleRepositoryImplTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("기사 검색 - 댓글 수와 조회 수를 집계한다")
    void countCommentsAndViews() {
        // given
        Article article = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article/1",
                "삼성전자 새로운 기술 발표",
                "삼성전자가 새로운 기술을 발표했다.",
                Instant.parse("2026-08-18T01:00:00Z")
        );

        // 서로 다른 사용자 3명
        Instant now = Instant.parse("2026-08-18T01:00:00Z");

        User user1 = new User(
                "user1@test.com",
                "user1",
                "password",
                now
        );

        User user2 = new User(
                "user2@test.com",
                "user2",
                "password",
                now
        );

        User user3 = new User(
                "user3@test.com",
                "user3",
                "password",
                now
        );

        em.persist(article);

        em.persist(user1);
        em.persist(user2);
        em.persist(user3);

        em.flush();

        // 댓글 2개
        Comment comment1 = new Comment(
                article,
                user1,
                "첫 번째 댓글입니다."
        );

        Comment comment2 = new Comment(
                article,
                user2,
                "두 번째 댓글입니다."
        );

        em.persist(comment1);
        em.persist(comment2);

        // 조회 3개
        ArticleView view1 = ArticleView.create(article, user1);
        ArticleView view2 = ArticleView.create(article, user2);
        ArticleView view3 = ArticleView.create(article, user3);

        em.persist(view1);
        em.persist(view2);
        em.persist(view3);

        em.flush();
        em.clear();

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
                10,
                UUID.randomUUID()
        );

        // when
        List<ArticleSearchResult> results =
                articleRepository.searchArticles(command);

        // then
        assertThat(results).hasSize(1);

        ArticleSearchResult result = results.get(0);

        assertThat(result.article().getTitle())
                .isEqualTo("삼성전자 새로운 기술 발표");

        assertThat(result.commentCount())
                .isEqualTo(2L);

        assertThat(result.viewCount())
                .isEqualTo(3L);
    }

    @Test
    @DisplayName("기사 키워드/출처/날짜 검색")
    void countTotalElementsByKeyword() {
        // given
        // Spring 관련 Article 2개
        // JPA 관련 Article 1개
        Article article1 = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article-1",
                "Spring Boot 새로운 기능",
                "Spring Boot 관련 새로운 기능을 소개하는 기사입니다.",
                Instant.parse("2026-08-01T10:00:00Z")
        );

        Article article2 = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article-2",
                "JPA 성능 최적화",
                "JPA를 활용한 데이터베이스 성능 최적화 방법을 소개합니다.",
                Instant.parse("2026-08-05T10:00:00Z")
        );

        Article article3 = Article.create(
                ArticleSource.HANKYUNG,
                "https://example.com/article-3",
                "Spring Data JPA 활용법",
                "Spring Data JPA의 다양한 활용 방법을 소개합니다.",
                Instant.parse("2026-08-10T10:00:00Z")
        );

        articleRepository.saveAll(List.of(article1, article2, article3));

        Interest interest = new Interest(
                "개발",
                List.of("Spring", "JPA")
        );

        interestRepository.save(interest);

        ArticleSearchCommand command = new ArticleSearchCommand(
                "Spring",
                null,
                ArticleSource.NAVER,
                Instant.parse("2026-07-31T00:00:00Z"),
                Instant.parse("2026-08-02T00:00:00Z"),
                null,
                null,
                null,
                null,
                null,
                null
        );

        // when
        long result = articleRepository.countTotalElements(command);

        // then
        assertThat(result).isEqualTo(1);
    }

}
