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
    @DisplayName("댓글 수와 조회 수 집계")
    void countCommentsAndViews() {
        // given
        Article article = createArticle(
                "https://example.com/article/1",
                "삼성전자 새로운 기술 발표",
                "2026-08-18T01:00:00Z"
        );

        User user = new User("user1@test.com", "user1", "password");
        User user2 = new User("user2@test.com", "user2", "password");
        User user3 = new User("user3@test.com", "user3", "password");

        em.persist(article);
        em.persist(user);
        em.persist(user2);
        em.persist(user3);

        em.flush();

        // 댓글 2개
        Comment comment1 = new Comment(article, user, "첫 번째 댓글입니다.");
        Comment comment2 = new Comment(article, user, "두 번째 댓글입니다.");

        em.persist(comment1);
        em.persist(comment2);

        // 조회 3개
        ArticleView view1 = ArticleView.create(article, user);
        ArticleView view2 = ArticleView.create(article, user2);
        ArticleView view3 = ArticleView.create(article, user3);

        em.persist(view1);
        em.persist(view2);
        em.persist(view3);

        em.flush();
        em.clear();

        ArticleSearchCommand command = searchCommand(null, null, "publishDate", "desc", 2, user.getId());

        // when
        List<ArticleSearchResult> results =
                articleRepository.searchArticles(command, command.orderBy());

        // then
        assertThat(results).hasSize(1);

        ArticleSearchResult result = results.get(0);

        assertThat(result.article().getTitle()).isEqualTo("삼성전자 새로운 기술 발표");
        assertThat(result.commentCount()).isEqualTo(2L);
        assertThat(result.viewCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("기사 키워드/출처/날짜 검색")
    void countTotalElementsByKeyword() {
        // given
        // Spring 관련 Article 2개
        // JPA 관련 Article 1개
        Article article1 = createArticle(
                "https://example.com/article-1",
                "Spring Boot 새로운 기능",
                "2026-08-01T10:00:00Z"
        );

        Article article2 = createArticle(
                "https://example.com/article-2",
                "Spring JPA 활용",
                "2026-08-05T10:00:00Z"
        );

        Article article3 = Article.create(
                ArticleSource.HANKYUNG,
                "https://example.com/article-3",
                "Spring Data JPA 활용법",
                "Spring Data JPA 관련 기사입니다.",
                Instant.parse("2026-08-10T10:00:00Z")
        );

        Article article4 = createArticle(
                "https://example.com/article-4",
                "Spring 오래된 기사",
                "2026-07-01T10:00:00Z"
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
    @DisplayName("댓글 수 정렬 시 두 번째 페이지 조회")
    void searchArticlesByCommentCountSecondPage() {
        // given
        Article article1 = createArticle(
                "https://example.com/article-1",
                "Spring Boot 새로운 기능",
                "2026-08-01T10:00:00Z"
        );

        Article article2 = createArticle(
                "https://example.com/article-2",
                "Spring JPA 활용",
                "2026-08-05T10:00:00Z"
        );

        Article article3 = Article.create(
                ArticleSource.HANKYUNG,
                "https://example.com/article-3",
                "Spring Data JPA 활용법",
                "Spring Data JPA 관련 기사입니다.",
                Instant.parse("2026-08-10T10:00:00Z")
        );

        articleRepository.saveAll(List.of(article1, article2, article3));

        User user1 = new User("user1@test.com", "user1", "password");
        User user2 = new User("user2@test.com", "user2", "password");
        User user3 = new User("user3@test.com", "user3", "password");

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

        ArticleSearchCommand firstPageCommand = searchCommand(
                null,
                null,
                "commentCount",
                "desc",
                2,
                user1.getId()
        );

        // when
        List<ArticleSearchResult> firstPage =
                articleRepository.searchArticles(firstPageCommand, firstPageCommand.orderBy());

        ArticleSearchCommand secondPageCommand = searchCommand(
                "2",
                article2.getId(),
                "commentCount",
                "desc",
                2,
                user1.getId()
        );

        // when
        List<ArticleSearchResult> secondPage = articleRepository.searchArticles(secondPageCommand, secondPageCommand.orderBy());

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).article().getId()).isEqualTo(article1.getId());
    }

    @Test
    @DisplayName("publishDate 정렬 시 커서 페이지네이션 정상 동작 검증")
    void searchArticles_orderByNull_cursorPagination() {
        // given
        Article article1 = createArticle(
                "https://example.com/1",
                "Article 1",
                "2026-08-01T10:00:00Z"
        );

        Article article2 = createArticle(
                "https://example.com/2",
                "Article 2",
                "2026-08-02T10:00:00Z"
        );

        Article article3 = createArticle(
                "https://example.com/3",
                "Article 3",
                "2026-08-03T10:00:00Z"
        );

        articleRepository.saveAll(List.of(article1, article2, article3));

        UUID userId = UUID.randomUUID();

        ArticleSearchCommand firstCommand = searchCommand(
                null,
                null,
                "publishDate",
                "desc",
                1,
                userId
        );

        // when
        List<ArticleSearchResult> firstPage =
                articleRepository.searchArticles(firstCommand, "publishDate");

        // then
        assertThat(firstPage).hasSize(2); // limit + 1
        assertThat(firstPage.get(0).article().getId()).isEqualTo(article3.getId());

        ArticleSearchResult last = firstPage.get(0);

        ArticleSearchCommand secondCommand = searchCommand(
                last.article().getPublishDate().toString(),
                last.article().getId(),
                null,
                "desc",
                1,
                userId
        );

        // when
        List<ArticleSearchResult> secondPage =
                articleRepository.searchArticles(secondCommand, "publishDate");

        // then
        assertThat(secondPage).hasSize(2); // limit + 1
        assertThat(secondPage.get(0).article().getId()).isEqualTo(article2.getId());
    }

    @Test
    @DisplayName("기사 검색 시 현재 사용자의 조회 여부를 반환한다")
    void searchArticles_viewedByMe() {
        // given
        Article article1 = createArticle(
                "https://example.com/1",
                "Article 1",
                "2026-08-01T10:00:00Z"
        );

        Article article2 = createArticle(
                "https://example.com/2",
                "Article 2",
                "2026-08-02T10:00:00Z"
        );

        articleRepository.saveAll(List.of(article1, article2));

        Instant now = Instant.parse("2026-08-18T01:00:00Z");

        User user = new User("user@test.com", "user", "password");
        User user2 = new User("user2@test.com", "user2", "password");

        em.persist(user);
        em.persist(user2);
        em.flush();

        // user가 article1만 조회함
        ArticleView articleView = ArticleView.create(article1, user);
        ArticleView articleView2 = ArticleView.create(article2, user2);

        em.persist(articleView);
        em.persist(articleView2);

        em.flush();
        em.clear();

        ArticleSearchCommand command = searchCommand(
                null,
                null,
                "publishDate",
                "desc",
                10,
                user.getId()
        );

        // when
        List<ArticleSearchResult> results =
                articleRepository.searchArticles(command, command.orderBy());

        // then
        assertThat(results).hasSize(2);

        ArticleSearchResult article1Result = results.stream()
                .filter(result -> result.article().getId().equals(article1.getId()))
                .findFirst()
                .orElseThrow();

        ArticleSearchResult article2Result = results.stream()
                .filter(result -> result.article().getId().equals(article2.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(article1Result.viewedByMe()).isTrue();
        assertThat(article2Result.viewedByMe()).isFalse();
    }

    // article 생성 헬퍼
    private Article createArticle(String url, String title, String publishDate) {
        return Article.create(
                ArticleSource.NAVER,
                url,
                title,
                title + " summary",
                Instant.parse(publishDate)
        );
    }

    // command 생성 헬퍼
    private ArticleSearchCommand searchCommand(
            String cursor,
            UUID after,
            String orderBy,
            String direction,
            int limit,
            UUID userId
    ) {
        return new ArticleSearchCommand(
                null,
                null,
                null,
                null,
                null,
                cursor,
                after,
                orderBy,
                direction,
                limit,
                userId
        );
    }

}
