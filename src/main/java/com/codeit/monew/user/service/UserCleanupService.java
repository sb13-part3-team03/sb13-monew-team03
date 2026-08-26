package com.codeit.monew.user.service;

import com.codeit.monew.article.repository.ArticleViewRepository;
import com.codeit.monew.comment.repository.CommentLikeRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.notification.repository.NotificationRepository;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import com.codeit.monew.useractivity.event.UserActivityEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCleanupService {

    private final UserRepository userRepository;
    private final ArticleViewRepository articleViewRepository;
    private final NotificationRepository notificationRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserActivityEventPublisher activityEvents;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupDeletedUsers() {

        Instant threshold = Instant.now()
                .minus(1, ChronoUnit.DAYS);

        List<User> users =
                userRepository
                        .findAllByDeletedAtIsNotNullAndDeletedAtLessThanEqual(threshold);

        if (users.isEmpty()) {
            log.info("물리 삭제 대상 사용자가 없습니다.");
            return;
        }

        log.info("물리 삭제 대상 사용자 수: {}", users.size());

        for (User user : users) {

            UUID userId = user.getId();

            // 탈퇴한 사용자가 작성한 댓글에 달린 좋아요 삭제
            commentLikeRepository.deleteAllByComment_User_Id(userId);

            // 탈퇴한 사용자가 직접 누른 댓글 좋아요 삭제
            commentLikeRepository.deleteAllByUser_Id(userId);

            // 탈퇴한 사용자가 작성한 댓글 삭제
            commentRepository.deleteAllByUser_Id(userId);

            // 사용자 기사 조회 기록 삭제
            articleViewRepository.deleteAllByUser_Id(userId);

            // 사용자 알림 삭제
            notificationRepository.deleteAllByUserId(userId);

            // 모든 연관 데이터를 정리한 뒤 사용자 물리 삭제
            userRepository.delete(user);

            // 트랜잭션 커밋 후 사용자의 활동내역을 삭제하도록 이벤트 발행
            activityEvents.userRemoved(userId);

            log.info("사용자 물리 삭제 완료: {}", userId);
        }
    }
}
