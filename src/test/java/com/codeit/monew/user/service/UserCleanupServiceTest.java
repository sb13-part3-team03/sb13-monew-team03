package com.codeit.monew.user.service;

import com.codeit.monew.article.repository.ArticleViewRepository;
import com.codeit.monew.comment.repository.CommentLikeRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.notification.repository.NotificationRepository;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import com.codeit.monew.useractivity.event.UserActivityEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserCleanupService 단위 테스트")
class UserCleanupServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArticleViewRepository articleViewRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private UserActivityEventPublisher activityEvents;

    @InjectMocks
    private UserCleanupService userCleanupService;

    @Test
    @DisplayName("물리 삭제 대상 사용자가 있으면 연관 데이터를 삭제한 후 사용자를 삭제한다.")
    void cleanupDeletedUsers_whenTargetUserExists_deletesRelatedDataAndUser() {

        // given
        User user = mock(User.class);
        UUID userId = UUID.randomUUID();

        given(user.getId())
                .willReturn(userId);

        given(userRepository
                .findAllByDeletedAtIsNotNullAndDeletedAtLessThanEqual(any(Instant.class)))
                .willReturn(List.of(user));

        // when
        userCleanupService.cleanupDeletedUsers();

        // then
        verify(commentLikeRepository)
                .deleteAllByComment_User_Id(userId);

        verify(commentLikeRepository)
                .deleteAllByUser_Id(userId);

        verify(commentRepository)
                .deleteAllByUser_Id(userId);

        verify(articleViewRepository)
                .deleteAllByUser_Id(userId);

        verify(notificationRepository)
                .deleteAllByUserId(userId);

        verify(userRepository)
                .delete(user);

        verify(activityEvents)
                .userRemoved(userId);
    }

    @Test
    @DisplayName("물리 삭제 대상 사용자가 없으면 삭제 작업을 수행하지 않는다.")
    void cleanupDeletedUsers_whenTargetUserDoesNotExist_doesNotDelete() {

        // given
        given(userRepository
                .findAllByDeletedAtIsNotNullAndDeletedAtLessThanEqual(any(Instant.class)))
                .willReturn(List.of());

        // when
        userCleanupService.cleanupDeletedUsers();

        // then
        verifyNoInteractions(
                articleViewRepository,
                notificationRepository,
                commentRepository,
                commentLikeRepository,
                activityEvents
        );

        verify(userRepository, never())
                .delete(any(User.class));
    }
}