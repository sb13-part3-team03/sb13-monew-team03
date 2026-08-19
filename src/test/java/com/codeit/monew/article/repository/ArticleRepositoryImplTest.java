package com.codeit.monew.article.repository;


import com.codeit.monew.article.dto.command.ArticleSearchCommand;
import com.codeit.monew.article.dto.response.ArticleSearchResult;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.article.entity.ArticleView;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.repository.CommentRepository;
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
    private CommentRepository commentRepository;

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
                "Spring JPA 활용",
                "Spring JPA 관련 기사입니다.",
                Instant.parse("2026-08-05T10:00:00Z")
        );

        Article article3 = Article.create(
                ArticleSource.HANKYUNG,
                "https://example.com/article-3",
                "Spring Data JPA 활용법",
                "Spring Data JPA 관련 기사입니다.",
                Instant.parse("2026-08-10T10:00:00Z")
        );

        Article article4 = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article-4",
                "Spring 오래된 기사",
                "Spring 관련 기사입니다.",
                Instant.parse("2026-07-01T10:00:00Z")
        );

        articleRepository.saveAll(List.of(article1, article2, article3, article4));

        Interest interest = new Interest(
                "개발",
                List.of("Spring", "JPA")
        );

        interestRepository.save(interest);

        ArticleSearchCommand command = new ArticleSearchCommand(
                "Spring",
                null,
                List.of(ArticleSource.NAVER),
                Instant.parse("2026-07-31T00:00:00Z"),
                Instant.parse("2026-08-06T00:00:00Z"),
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
        assertThat(result).isEqualTo(2);
    }

    @Test
    @DisplayName("댓글 수 정렬 시 두 번째 페이지를 조회한다")
    void searchArticlesByCommentCountSecondPage() {
        // given
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
                "Spring JPA 활용",
                "Spring JPA 관련 기사입니다.",
                Instant.parse("2026-08-05T10:00:00Z")
        );

        Article article3 = Article.create(
                ArticleSource.HANKYUNG,
                "https://example.com/article-3",
                "Spring Data JPA 활용법",
                "Spring Data JPA 관련 기사입니다.",
                Instant.parse("2026-08-10T10:00:00Z")
        );

        articleRepository.saveAll(List.of(article1, article2, article3));

        // 서로 다른 사용자 3명
        Instant now = Instant.parse("2026-08-18T01:00:00Z");

        User user1 = new User("user1@test.com", "user1", "password", now);
        User user2 = new User("user2@test.com", "user2", "password", now);
        User user3 = new User("user3@test.com", "user3", "password", now);

        em.persist(user1);
        em.persist(user2);
        em.persist(user3);

        // article1 → 댓글 1개
        // article2 → 댓글 2개
        // article3 → 댓글 3개
        Comment comment1 = new Comment(article1, user1, "첫 번째 댓글입니다.");
        Comment comment2 = new Comment(article2, user2, "두 번째 댓글입니다.");
        Comment comment3 = new Comment(article2, user1, "세 번째 댓글입니다.");
        Comment comment4 = new Comment(article3, user3, "네 번째 댓글입니다.");
        Comment comment5 = new Comment(article3, user3, "다섯 번째 댓글입니다.");
        Comment comment6 = new Comment(article3, user3, "여섯 번째 댓글입니다.");

        commentRepository.saveAll(List.of(comment1, comment2, comment3, comment4, comment5, comment6));

        ArticleSearchCommand firstPageCommand = new ArticleSearchCommand(
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
                user1.getId()
        );

        // when
        List<ArticleSearchResult> firstPage =
                articleRepository.searchArticles(firstPageCommand);

        ArticleSearchResult lastArticle = firstPage.get(1);

        String cursor = "2";
        UUID after = article2.getId();

        ArticleSearchCommand secondPageCommand = new ArticleSearchCommand(
                null,
                null,
                null,
                null,
                null,
                cursor,
                after,
                "commentCount",
                "desc",
                2,
                user1.getId()
        );

        // when
        List<ArticleSearchResult> secondPage = articleRepository.searchArticles(secondPageCommand);

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).article().getId()).isEqualTo(article1.getId());
    }

}
