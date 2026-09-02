package com.codeit.monew.useractivity.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeit.monew.article.dto.response.ArticleViewDto;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentLike;
import com.codeit.monew.comment.repository.CommentLikeRepository;
import com.codeit.monew.user.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 활동 이벤트 발행기 테스트")
class UserActivityEventPublisherTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @Mock
  private CommentLikeRepository commentLikeRepository;

  @InjectMocks
  private UserActivityEventPublisher publisher;

  @Mock
  private User user;

  private UUID userId;
  private Instant userCreatedAt;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    userCreatedAt = Instant.parse("2026-09-01T00:00:00Z");
    lenient().when(user.getId()).thenReturn(userId);
    lenient().when(user.getEmail()).thenReturn("user@example.com");
    lenient().when(user.getNickname()).thenReturn("nickname");
    lenient().when(user.getCreatedAt()).thenReturn(userCreatedAt);
  }

  @Test
  @DisplayName("프로필 변경 이벤트에 사용자 스냅샷을 담아 발행")
  void profileChanged() {
    publisher.profileChanged(user);

    UserActivityEvent.Profile event = captureSingleEvent(UserActivityEvent.Profile.class);
    assertProfile(event);
  }

  @Test
  @DisplayName("댓글 추가 이벤트에 댓글과 작성자 정보를 담아 발행")
  void commentAdded() {
    Comment comment = org.mockito.Mockito.mock(Comment.class);
    Article article = org.mockito.Mockito.mock(Article.class);
    UUID commentId = UUID.randomUUID();
    UUID articleId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-09-01T01:00:00Z");
    given(comment.getUser()).willReturn(user);
    given(comment.getArticle()).willReturn(article);
    given(comment.getId()).willReturn(commentId);
    given(comment.getContent()).willReturn("comment");
    given(comment.getCreatedAt()).willReturn(createdAt);
    given(article.getId()).willReturn(articleId);
    given(article.getTitle()).willReturn("article");

    publisher.commentAdded(comment, 3L);

    UserActivityEvent.CommentAdded event =
        captureSingleEvent(UserActivityEvent.CommentAdded.class);
    assertProfile(event.user());
    assertThat(event.comment().id()).isEqualTo(commentId);
    assertThat(event.comment().articleId()).isEqualTo(articleId);
    assertThat(event.comment().articleTitle()).isEqualTo("article");
    assertThat(event.comment().userId()).isEqualTo(userId);
    assertThat(event.comment().userNickname()).isEqualTo("nickname");
    assertThat(event.comment().content()).isEqualTo("comment");
    assertThat(event.comment().likeCount()).isEqualTo(3L);
    assertThat(event.comment().createdAt()).isEqualTo(createdAt);
  }

  @Test
  @DisplayName("댓글 수정 이벤트를 발행")
  void commentUpdated() {
    Comment comment = org.mockito.Mockito.mock(Comment.class);
    UUID commentId = UUID.randomUUID();
    given(comment.getId()).willReturn(commentId);
    given(comment.getContent()).willReturn("updated");

    publisher.commentUpdated(comment);

    UserActivityEvent.CommentUpdated event =
        captureSingleEvent(UserActivityEvent.CommentUpdated.class);
    assertThat(event.commentId()).isEqualTo(commentId);
    assertThat(event.content()).isEqualTo("updated");
  }

  @Test
  @DisplayName("댓글 삭제 이벤트를 발행")
  void commentRemoved() {
    UUID commentId = UUID.randomUUID();

    publisher.commentRemoved(commentId);

    UserActivityEvent.CommentRemoved event =
        captureSingleEvent(UserActivityEvent.CommentRemoved.class);
    assertThat(event.commentId()).isEqualTo(commentId);
  }

  @Test
  @DisplayName("좋아요 추가와 댓글 좋아요 수 변경 이벤트를 함께 발행")
  void likeAdded() {
    CommentLike like = org.mockito.Mockito.mock(CommentLike.class);
    Comment comment = org.mockito.Mockito.mock(Comment.class);
    Article article = org.mockito.Mockito.mock(Article.class);
    User writer = org.mockito.Mockito.mock(User.class);
    UUID likeId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();
    UUID articleId = UUID.randomUUID();
    UUID writerId = UUID.randomUUID();
    Instant likeCreatedAt = Instant.parse("2026-09-01T02:00:00Z");
    Instant commentCreatedAt = Instant.parse("2026-08-31T00:00:00Z");
    given(like.getUser()).willReturn(user);
    given(like.getComment()).willReturn(comment);
    given(like.getId()).willReturn(likeId);
    given(like.getCreatedAt()).willReturn(likeCreatedAt);
    given(comment.getId()).willReturn(commentId);
    given(comment.getArticle()).willReturn(article);
    given(comment.getUser()).willReturn(writer);
    given(comment.getContent()).willReturn("liked comment");
    given(comment.getCreatedAt()).willReturn(commentCreatedAt);
    given(article.getId()).willReturn(articleId);
    given(article.getTitle()).willReturn("article");
    given(writer.getId()).willReturn(writerId);
    given(writer.getNickname()).willReturn("writer");

    publisher.likeAdded(like, 5L);

    List<Object> events = captureEvents(2);
    UserActivityEvent.LikeAdded added =
        (UserActivityEvent.LikeAdded) events.get(0);
    UserActivityEvent.LikeCountChanged changed =
        (UserActivityEvent.LikeCountChanged) events.get(1);
    assertProfile(added.user());
    assertThat(added.like().id()).isEqualTo(likeId);
    assertThat(added.like().createdAt()).isEqualTo(likeCreatedAt);
    assertThat(added.like().commentId()).isEqualTo(commentId);
    assertThat(added.like().articleId()).isEqualTo(articleId);
    assertThat(added.like().articleTitle()).isEqualTo("article");
    assertThat(added.like().commentUserId()).isEqualTo(writerId);
    assertThat(added.like().commentUserNickname()).isEqualTo("writer");
    assertThat(added.like().commentContent()).isEqualTo("liked comment");
    assertThat(added.like().commentLikeCount()).isEqualTo(5L);
    assertThat(added.like().commentCreatedAt()).isEqualTo(commentCreatedAt);
    assertThat(changed.commentId()).isEqualTo(commentId);
    assertThat(changed.count()).isEqualTo(5L);
  }

  @Test
  @DisplayName("좋아요 삭제 후 레포지토리에서 다시 계산한 좋아요 수를 발행")
  void likeRemoved() {
    UUID likeId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();
    given(commentLikeRepository.countByComment_Id(commentId)).willReturn(4L);

    publisher.likeRemoved(userId, likeId, commentId);

    List<Object> events = captureEvents(2);
    UserActivityEvent.LikeRemoved removed =
        (UserActivityEvent.LikeRemoved) events.get(0);
    UserActivityEvent.LikeCountChanged changed =
        (UserActivityEvent.LikeCountChanged) events.get(1);
    assertThat(removed.userId()).isEqualTo(userId);
    assertThat(removed.likeId()).isEqualTo(likeId);
    assertThat(changed.commentId()).isEqualTo(commentId);
    assertThat(changed.count()).isEqualTo(4L);
  }

  @Test
  @DisplayName("기사 조회 이벤트에 사용자 프로필과 조회 정보를 담아 발행")
  void articleViewed() {
    ArticleViewDto view = new ArticleViewDto(
        UUID.randomUUID(), userId, Instant.now(), UUID.randomUUID(), "NAVER",
        "https://example.com", "article", Instant.now(), "summary", 0L, 1L);

    publisher.articleViewed(user, view);

    UserActivityEvent.ArticleViewed event =
        captureSingleEvent(UserActivityEvent.ArticleViewed.class);
    assertProfile(event.user());
    assertThat(event.view()).isSameAs(view);
  }

  @Test
  @DisplayName("사용자 삭제 이벤트를 발행")
  void userRemoved() {
    publisher.userRemoved(userId);

    UserActivityEvent.UserRemoved event =
        captureSingleEvent(UserActivityEvent.UserRemoved.class);
    assertThat(event.userId()).isEqualTo(userId);
  }

  private <T> T captureSingleEvent(Class<T> eventType) {
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(applicationEventPublisher).publishEvent(captor.capture());
    assertThat(captor.getValue()).isInstanceOf(eventType);
    return eventType.cast(captor.getValue());
  }

  private List<Object> captureEvents(int count) {
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(applicationEventPublisher, times(count)).publishEvent(captor.capture());
    return captor.getAllValues();
  }

  private void assertProfile(UserActivityEvent.Profile profile) {
    assertThat(profile.id()).isEqualTo(userId);
    assertThat(profile.email()).isEqualTo("user@example.com");
    assertThat(profile.nickname()).isEqualTo("nickname");
    assertThat(profile.createdAt()).isEqualTo(userCreatedAt);
  }
}
