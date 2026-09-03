package com.codeit.monew.useractivity.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.monew.article.dto.response.ArticleViewDto;
import com.codeit.monew.useractivity.dto.response.CommentActivityDto;
import com.codeit.monew.useractivity.dto.response.CommentLikeActivityDto;
import com.codeit.monew.useractivity.entity.UserActivity;
import com.codeit.monew.useractivity.event.UserActivityEvent;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;

@ExtendWith(MockitoExtension.class)
@DisplayName("활동내역 프로젝션 서비스 테스트")
class UserActivityProjectionServiceTest {

  @Mock
  private MongoTemplate mongoTemplate;

  @InjectMocks
  private UserActivityProjectionService projectionService;

  @Test
  @DisplayName("프로필 이벤트를 활동내역에 반영")
  void applyProfile() {
    UserActivityEvent.Profile event = new UserActivityEvent.Profile(
        UUID.randomUUID(), "user@example.com", "nickname", Instant.now());

    projectionService.apply(event);

    verify(mongoTemplate).upsert(any(Query.class), any(UpdateDefinition.class),
        eq(UserActivity.class));
    verify(mongoTemplate).updateFirst(any(Query.class), any(UpdateDefinition.class),
        eq(UserActivity.class));
  }

  @Test
  @DisplayName("댓글 추가 이벤트를 최근 활동에 반영")
  void applyCommentAdded() {
    UserActivityEvent.Profile profile = profile();
    CommentActivityDto comment = new CommentActivityDto(
        UUID.randomUUID(), UUID.randomUUID(), "article", profile.id(), profile.nickname(),
        "content", 0, Instant.now());

    projectionService.apply(new UserActivityEvent.CommentAdded(profile, comment));

    verify(mongoTemplate).upsert(any(Query.class), any(UpdateDefinition.class),
        eq(UserActivity.class));
    verify(mongoTemplate).updateFirst(any(Query.class), any(UpdateDefinition.class),
        eq(UserActivity.class));
  }

  @Test
  @DisplayName("좋아요 추가 이벤트를 최근 활동에 반영")
  void applyLikeAdded() {
    UserActivityEvent.Profile profile = profile();
    CommentLikeActivityDto like = new CommentLikeActivityDto(
        UUID.randomUUID(), Instant.now(), UUID.randomUUID(), UUID.randomUUID(), "article",
        UUID.randomUUID(), "writer", "comment", 1, Instant.now());

    projectionService.apply(new UserActivityEvent.LikeAdded(profile, like));

    verify(mongoTemplate).upsert(any(Query.class), any(UpdateDefinition.class),
        eq(UserActivity.class));
    verify(mongoTemplate).updateFirst(any(Query.class), any(UpdateDefinition.class),
        eq(UserActivity.class));
  }

  @Test
  @DisplayName("기사 조회 이벤트를 최근 활동에 반영")
  void applyArticleViewed() {
    UserActivityEvent.Profile profile = profile();
    ArticleViewDto view = new ArticleViewDto(
        UUID.randomUUID(), profile.id(), Instant.now(), UUID.randomUUID(), "NAVER",
        "https://example.com", "article", Instant.now(), "summary", 0L, 1L);

    projectionService.apply(new UserActivityEvent.ArticleViewed(profile, view));

    verify(mongoTemplate).upsert(any(Query.class), any(UpdateDefinition.class),
        eq(UserActivity.class));
    verify(mongoTemplate).updateFirst(any(Query.class), any(UpdateDefinition.class),
        eq(UserActivity.class));
  }

  @Test
  @DisplayName("댓글 수정 이벤트를 댓글과 좋아요 활동에 모두 반영")
  void applyCommentUpdated() {
    projectionService.apply(new UserActivityEvent.CommentUpdated(UUID.randomUUID(), "updated"));

    verify(mongoTemplate, times(2)).updateMulti(any(Query.class),
        any(UpdateDefinition.class), eq(UserActivity.class));
  }

  @Test
  @DisplayName("댓글 좋아요 수 변경을 댓글과 좋아요 활동에 모두 반영")
  void applyLikeCountChanged() {
    projectionService.apply(new UserActivityEvent.LikeCountChanged(UUID.randomUUID(), 3));

    verify(mongoTemplate, times(2)).updateMulti(any(Query.class),
        any(UpdateDefinition.class), eq(UserActivity.class));
  }

  @Test
  @DisplayName("댓글 삭제 이벤트를 댓글과 좋아요 활동에서 제거")
  void applyCommentRemoved() {
    projectionService.apply(new UserActivityEvent.CommentRemoved(UUID.randomUUID()));

    verify(mongoTemplate).updateMulti(any(Query.class), any(UpdateDefinition.class),
        eq(UserActivity.class));
  }

  @Test
  @DisplayName("좋아요 삭제 이벤트를 사용자 활동에서 제거")
  void applyLikeRemoved() {
    projectionService.apply(
        new UserActivityEvent.LikeRemoved(UUID.randomUUID(), UUID.randomUUID()));

    verify(mongoTemplate).updateFirst(any(Query.class), any(UpdateDefinition.class),
        eq(UserActivity.class));
  }

  @Test
  @DisplayName("사용자 삭제 이벤트로 활동내역을 삭제 상태로 변경")
  void applyUserRemoved() {
    projectionService.apply(new UserActivityEvent.UserRemoved(UUID.randomUUID()));

    verify(mongoTemplate).upsert(any(Query.class), any(UpdateDefinition.class),
        eq(UserActivity.class));
  }

  @Test
  @DisplayName("사용자 삭제 중 동시 삽입이 발생하면 기존 활동내역을 갱신")
  void applyUserRemoved_whenDuplicateKey_updatesExistingActivity() {
    doThrow(new DuplicateKeyException("duplicate"))
        .when(mongoTemplate)
        .upsert(any(Query.class), any(UpdateDefinition.class), eq(UserActivity.class));

    projectionService.apply(new UserActivityEvent.UserRemoved(UUID.randomUUID()));

    verify(mongoTemplate).updateFirst(any(Query.class), any(UpdateDefinition.class),
        eq(UserActivity.class));
  }

  @Test
  @DisplayName("프로필 동시 생성 충돌 시 이미 생성된 활동내역이면 계속 처리")
  void applyProfile_whenDuplicateKeyAndActivityExists_continues() {
    doThrow(new DuplicateKeyException("duplicate"))
        .when(mongoTemplate)
        .upsert(any(Query.class), any(UpdateDefinition.class), eq(UserActivity.class));
    when(mongoTemplate.exists(any(Query.class), eq(UserActivity.class))).thenReturn(true);

    projectionService.apply(profile());

    verify(mongoTemplate).updateFirst(any(Query.class), any(UpdateDefinition.class),
        eq(UserActivity.class));
  }

  @Test
  @DisplayName("프로필 동시 생성 충돌 후 활동내역이 없으면 예외를 다시 발생")
  void applyProfile_whenDuplicateKeyAndActivityDoesNotExist_throwsException() {
    doThrow(new DuplicateKeyException("duplicate"))
        .when(mongoTemplate)
        .upsert(any(Query.class), any(UpdateDefinition.class), eq(UserActivity.class));
    when(mongoTemplate.exists(any(Query.class), eq(UserActivity.class))).thenReturn(false);

    org.junit.jupiter.api.Assertions.assertThrows(DuplicateKeyException.class,
        () -> projectionService.apply(profile()));

    verify(mongoTemplate, never()).updateFirst(any(Query.class), any(UpdateDefinition.class),
        eq(UserActivity.class));
  }

  private UserActivityEvent.Profile profile() {
    return new UserActivityEvent.Profile(
        UUID.randomUUID(), "user@example.com", "nickname", Instant.now());
  }

}
