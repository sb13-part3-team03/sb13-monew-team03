package com.codeit.monew.user.service;

import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCleanupService {

    private final UserRepository userRepository;

    /**
     * 매시간 정각 실행.
     * 논리 삭제(deletedAt 설정) 후 1일 이상 지난 사용자를 물리 삭제 대상으로 조회한다.
     */
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

            /*
             * TODO
             * 사용자 물리 삭제 전에 연관 데이터 삭제 필요
             *
             * - ArticleView: userId 기준 삭제
             * - Notification: userId 기준 삭제
             * - Comment / CommentLike: cascade 처리 예정
             *
             * 관련 도메인 삭제 메서드가 develop에 반영된 후
             * 여기에서 연관 데이터 삭제를 먼저 호출한다.
             */

            // 연관 데이터 삭제 연결 후 활성화
            // userRepository.delete(user);

            log.info("물리 삭제 예정 사용자: {}", user.getId());
        }
    }
}