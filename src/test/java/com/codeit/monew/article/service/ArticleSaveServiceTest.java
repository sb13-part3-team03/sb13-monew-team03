package com.codeit.monew.article.service;

import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleInterest;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.article.repository.ArticleInterestRepository;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Subscription;
import com.codeit.monew.interest.repository.SubscriptionRepository;
import com.codeit.monew.notification.enums.ResourceType;
import com.codeit.monew.notification.service.NotificationService;
import com.codeit.monew.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ArticleSaveServiceTest {

    @Mock
    private Subscription subscription;

    @Mock
    private User user;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleInterestRepository articleInterestRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private NotificationService notificationService;

    private ArticleSaveService articleSaveService;

    @BeforeEach
    void setUp() {
        articleSaveService = new ArticleSaveService(
                articleRepository,
                articleInterestRepository,
                subscriptionRepository,
                notificationService
        );
    }

    @Test
    @DisplayName("새로운 기사라면 저장한다.")
    void saveOneArticle_whenNewArticle_savesArticle() {

        // given
        Interest interest = createInterest();
        CollectedArticleDTO dto = createCollectedArticle();

        given(articleRepository.findBySourceUrl(dto.sourceUrl()))
                .willReturn(Optional.empty());

        given(articleRepository.save(any(Article.class)))
                .willAnswer(
                        invocation -> invocation.getArgument(0)
                );

        given(articleInterestRepository.existsById(any()))
                .willReturn(false);

        // when
        articleSaveService.saveOneArticle(
                dto,
                interest
        );

        // then
        verify(articleRepository)
                .save(any(Article.class));
    }

    @Test
    @DisplayName("이미 존재하는 기사라면 중복 저장하지 않는다.")
    void saveOneArticle_whenArticleAlreadyExists_doesNotSaveArticle() {

        // given
        Interest interest = createInterest();
        CollectedArticleDTO dto = createCollectedArticle();

        Article existingArticle = Article.create(
                dto.source(),
                dto.sourceUrl(),
                dto.title(),
                dto.summary(),
                dto.publishDate()
        );

        given(articleRepository.findBySourceUrl(dto.sourceUrl()))
                .willReturn(
                        Optional.of(existingArticle)
                );

        given(articleInterestRepository.existsById(any()))
                .willReturn(false);

        // when
        articleSaveService.saveOneArticle(
                dto,
                interest
        );

        // then
        verify(articleRepository, never())
                .save(any(Article.class));
    }

    @Test
    @DisplayName("기사와 관심사를 연결한다.")
    void saveOneArticle_whenNotLinked_savesArticleInterest() {

        // given
        Interest interest = createInterest();
        CollectedArticleDTO dto = createCollectedArticle();

        Article existingArticle = Article.create(
                dto.source(),
                dto.sourceUrl(),
                dto.title(),
                dto.summary(),
                dto.publishDate()
        );

        given(articleRepository.findBySourceUrl(dto.sourceUrl()))
                .willReturn(
                        Optional.of(existingArticle)
                );

        given(articleInterestRepository.existsById(any()))
                .willReturn(false);

        // when
        articleSaveService.saveOneArticle(
                dto,
                interest
        );

        // then
        verify(articleInterestRepository)
                .save(any(ArticleInterest.class));
    }

    @Test
    @DisplayName("기사와 관심사가 이미 연결되어 있으면 중복 저장하지 않는다.")
    void saveOneArticle_whenAlreadyLinked_doesNotSaveArticleInterest() {

        // given
        Interest interest = createInterest();
        CollectedArticleDTO dto = createCollectedArticle();

        Article existingArticle = Article.create(
                dto.source(),
                dto.sourceUrl(),
                dto.title(),
                dto.summary(),
                dto.publishDate()
        );

        given(articleRepository.findBySourceUrl(dto.sourceUrl()))
                .willReturn(
                        Optional.of(existingArticle)
                );

        given(articleInterestRepository.existsById(any()))
                .willReturn(true);

        // when
        articleSaveService.saveOneArticle(
                dto,
                interest
        );

        // then
        verify(articleInterestRepository, never())
                .save(any(ArticleInterest.class));
        verify(subscriptionRepository, never())
                .findAllByInterestId(any());
        verify(notificationService, never())
                .create(any(), any(), any(), any());
    }

    private Interest createInterest() {
        return new Interest(
                "인공지능",
                List.of("AI")
        );
    }

    private CollectedArticleDTO createCollectedArticle() {
        return new CollectedArticleDTO(
                ArticleSource.NAVER,
                "https://example.com/article/1",
                "AI 관련 뉴스",
                "AI 관련 뉴스 요약",
                Instant.parse("2026-08-18T03:00:00Z")
        );
    }

    @Test
    @DisplayName("새로운 기사와 관심사가 연결되면 해당 관심사의 구독자를 조회한다.")
    void saveOneArticle_whenNewlyLinked_findsSubscriptions() {

        // given
        Interest interest = createInterest();
        CollectedArticleDTO dto = createCollectedArticle();

        Article existingArticle = Article.create(
                dto.source(),
                dto.sourceUrl(),
                dto.title(),
                dto.summary(),
                dto.publishDate()
        );

        given(articleRepository.findBySourceUrl(dto.sourceUrl()))
                .willReturn(Optional.of(existingArticle));

        given(articleInterestRepository.existsById(any()))
                .willReturn(false);

        // when
        articleSaveService.saveOneArticle(dto, interest);

        // then
        verify(subscriptionRepository)
                .findAllByInterestId(interest.getId());
    }

    @Test
    @DisplayName("새로운 기사와 관심사가 연결되면 구독자에게 알림을 생성한다.")
    void saveOneArticle_whenNewlyLinked_createsNotification() {

        // given
        Interest interest = createInterest();
        CollectedArticleDTO dto = createCollectedArticle();

        Article existingArticle = Article.create(
                dto.source(),
                dto.sourceUrl(),
                dto.title(),
                dto.summary(),
                dto.publishDate()
        );

        UUID userId = UUID.randomUUID();

        given(articleRepository.findBySourceUrl(dto.sourceUrl()))
                .willReturn(Optional.of(existingArticle));

        given(articleInterestRepository.existsById(any()))
                .willReturn(false);

        given(subscriptionRepository.findAllByInterestId(interest.getId()))
                .willReturn(List.of(subscription));

        given(subscription.getUser())
                .willReturn(user);

        given(user.getId())
                .willReturn(userId);

        // when
        articleSaveService.saveOneArticle(dto, interest);

        // then
        verify(notificationService).create(
                existingArticle.getSummary(),
                userId,
                ResourceType.INTEREST,
                interest.getId()
        );
    }
}