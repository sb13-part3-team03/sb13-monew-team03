package com.codeit.monew.article.service;

import com.codeit.monew.article.dto.command.ArticleViewCreateCommand;
import com.codeit.monew.article.dto.response.ArticleViewDto;
import com.codeit.monew.article.dto.response.ArticleViewResultDto;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.article.entity.ArticleView;
import com.codeit.monew.article.exception.ArticleNotFoundException;
import com.codeit.monew.article.mapper.ArticleViewMapper;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.repository.ArticleViewRepository;
import com.codeit.monew.comment.repository.CommentRepository;

import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.exception.UserNotFoundException;
import com.codeit.monew.user.repository.UserRepository;
import com.codeit.monew.useractivity.event.UserActivityEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class ArticleViewServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleViewRepository articleViewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ArticleViewMapper articleViewMapper;

    @Mock
    private ArticleViewSaveService articleViewSaveService;

    // ArticleViewService에서 조회 이벤트를 발행하므로 테스트에서도 mock 객체를 주입
    @Mock
    private UserActivityEventPublisher activityEvents;

    @InjectMocks
    private ArticleViewService articleViewService;

    private Article article;
    private User user;
    private ArticleView articleView;

    @BeforeEach
    void setUp() {
        article = Article.create(
                ArticleSource.NAVER,
                "https://example.com/article",
                "테스트 기사",
                "테스트 요약",
                Instant.now()
        );

        user = new User(
                "user1@test.com",
                "user1",
                "password"
        );

        articleView = ArticleView.create(article, user);
    }

    private ArticleViewDto createExpectedDto() {
        return new ArticleViewDto(
                articleView.getId(),
                user.getId(),
                articleView.getCreatedAt(),
                article.getId(),
                "NAVER",
                "https://example.com/article",
                "테스트 기사",
                article.getPublishDate(),
                "테스트 요약",
                3L,
                5L
        );
    }

    private void assertArticleViewResult(
            ArticleView expectedArticleView,
            Long expectedCommentCount,
            Long expectedViewCount
    ) {
        ArgumentCaptor<ArticleViewResultDto> captor =
                ArgumentCaptor.forClass(ArticleViewResultDto.class);

        then(articleViewMapper)
                .should()
                .toDto(captor.capture());

        ArticleViewResultDto actual = captor.getValue();

        assertThat(actual.articleView())
                .isEqualTo(expectedArticleView);

        assertThat(actual.commentCount())
                .isEqualTo(expectedCommentCount);

        assertThat(actual.viewCount())
                .isEqualTo(expectedViewCount);
    }

    @Test
    @DisplayName("조회 기록이 없으면 ArticleView를 저장한다")
    void saveArticleView_whenNotExists() {
        // given
        given(articleRepository.findById(article.getId()))
                .willReturn(Optional.of(article));

        given(userRepository.findById(user.getId()))
                .willReturn(Optional.of(user));

        given(articleViewRepository.findByArticleIdAndUserId(
                article.getId(),
                user.getId()
        )).willReturn(Optional.empty());

        given(articleViewSaveService.save(article, user))
                .willReturn(articleView);

        given(commentRepository.countAllByDeletedAtIsNullAndArticleId(article.getId()))
                .willReturn(3L);

        given(articleViewRepository.countByArticleId(article.getId()))
                .willReturn(5L);

        ArticleViewDto expected = createExpectedDto();

        given(articleViewMapper.toDto(any(ArticleViewResultDto.class)))
                .willReturn(expected);

        ArticleViewCreateCommand command =
                new ArticleViewCreateCommand(article.getId(), user.getId());

        // when
        ArticleViewDto result = articleViewService.save(command);

        // then
        assertThat(result).isEqualTo(expected);

        assertArticleViewResult(articleView, 3L, 5L);

        then(articleViewSaveService)
                .should()
                .save(article, user);

        then(activityEvents)
                .should()
                .articleViewed(user, expected);
    }

    @Test
    @DisplayName("조회 기록이 이미 있으면 저장하지 않는다")
    void saveArticleView_whenAlreadyExists() {
        // given
        given(articleRepository.findById(article.getId()))
                .willReturn(Optional.of(article));

        given(userRepository.findById(user.getId()))
                .willReturn(Optional.of(user));

        given(articleViewRepository.findByArticleIdAndUserId(
                article.getId(),
                user.getId()
        )).willReturn(Optional.of(articleView));

        given(commentRepository.countAllByDeletedAtIsNullAndArticleId(
                article.getId()
        )).willReturn(3L);

        given(articleViewRepository.countByArticleId(
                article.getId()
        )).willReturn(5L);

        ArticleViewDto expected = createExpectedDto();

        given(articleViewMapper.toDto(any(ArticleViewResultDto.class)))
                .willReturn(expected);

        ArticleViewCreateCommand command =
                new ArticleViewCreateCommand(
                        article.getId(),
                        user.getId()
                );

        // when
        ArticleViewDto result = articleViewService.save(command);

        // then
        assertThat(result).isEqualTo(expected);

        assertArticleViewResult(articleView, 3L, 5L);

        then(articleViewSaveService)
                .should(never())
                .save(any(Article.class), any(User.class));

        then(activityEvents)
                .should()
                .articleViewed(user, expected);
    }

    @Test
    @DisplayName("존재하지 않는 articleId이면 ArticleNotFoundException이 발생한다")
    void saveArticleView_whenArticleNotFound() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = user.getId();

        given(articleRepository.findById(articleId))
                .willReturn(Optional.empty());

        ArticleViewCreateCommand command =
                new ArticleViewCreateCommand(articleId, userId);

        // when & then
        assertThatThrownBy(() -> articleViewService.save(command))
                .isInstanceOf(ArticleNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 userId이면 UserNotFoundException이 발생한다")
    void saveArticleView_whenUserNotFound() {
        // given
        UUID articleId = article.getId();
        UUID userId = UUID.randomUUID();

        given(articleRepository.findById(articleId))
                .willReturn(Optional.of(article));

        given(userRepository.findById(userId))
                .willReturn(Optional.empty());

        ArticleViewCreateCommand command =
                new ArticleViewCreateCommand(articleId, userId);

        // when & then
        assertThatThrownBy(() -> articleViewService.save(command))
                .isInstanceOf(UserNotFoundException.class);
    }

}
