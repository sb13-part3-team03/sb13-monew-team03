package com.codeit.monew.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.monew.global.config.QuerydslConfig;
import com.codeit.monew.notification.entity.Notification;
import com.codeit.monew.notification.enums.ResourceType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
@Import(QuerydslConfig.class)
@DisplayName("NotificationRepository 테스트")
class NotificationRepositoryTest {

  private static final Instant BASE_TIME = Instant.parse("2026-08-20T00:00:00Z");

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  @DisplayName("미확인 알림 조회")
  void findByUserIdAndConfirmedFalse() {
    UUID userId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    Notification expected = saveNotification(userId, false, BASE_TIME);
    saveNotification(userId, true, BASE_TIME.plusSeconds(1));
    saveNotification(otherUserId, false, BASE_TIME.plusSeconds(2));
    flushAndClear();

    List<Notification> result = notificationRepository.findByUserIdAndConfirmedFalse(userId);

    assertThat(result)
        .extracting(Notification::getId)
        .containsExactly(expected.getId());
  }

  @Test
  @DisplayName("미확인 알림 카운트")
  void countByUserIdAndConfirmedFalse() {
    UUID userId = UUID.randomUUID();
    saveNotification(userId, false, BASE_TIME);
    saveNotification(userId, false, BASE_TIME.plusSeconds(1));
    saveNotification(userId, true, BASE_TIME.plusSeconds(2));
    saveNotification(UUID.randomUUID(), false, BASE_TIME.plusSeconds(3));
    flushAndClear();

    long result = notificationRepository.countByUserIdAndConfirmedFalse(userId);

    assertThat(result).isEqualTo(2);
  }

  @Test
  @DisplayName("기준 시각보다 오래된 확인 알림만 삭제")
  void deleteConfirmedBefore() {
    Instant threshold = BASE_TIME.plusSeconds(10);
    Notification oldConfirmed = saveNotification(UUID.randomUUID(), true, BASE_TIME);
    Notification recentConfirmed =
        saveNotification(UUID.randomUUID(), true, threshold.plusSeconds(1));
    Notification oldNotConfirmed = saveNotification(UUID.randomUUID(), false, BASE_TIME);
    flushAndClear();

    int deletedCount = notificationRepository.deleteConfirmedBefore(threshold);

    assertThat(deletedCount).isEqualTo(1);
    assertThat(notificationRepository.findById(oldConfirmed.getId())).isEmpty();
    assertThat(notificationRepository.findById(recentConfirmed.getId())).isPresent();
    assertThat(notificationRepository.findById(oldNotConfirmed.getId())).isPresent();
  }

  @Test
  @DisplayName("커서보다 오래된 미확인 알림을 조회")
  void findAllNotConfirmedWithCursor() {
    UUID userId = UUID.randomUUID();
    Notification oldest = saveNotification(userId, false, BASE_TIME);
    Notification cursor = saveNotification(userId, false, BASE_TIME.plusSeconds(1));
    saveNotification(userId, false, BASE_TIME.plusSeconds(2));
    flushAndClear();

    List<Notification> result = notificationRepository.findAllNotConfirmed(
        userId, BASE_TIME.plusSeconds(1), cursor.getId(), 10);

    assertThat(result)
        .extracting(Notification::getId)
        .containsExactly(oldest.getId());
  }

  private Notification saveNotification(UUID userId, boolean confirmed, Instant timestamp) {
    Notification notification = new Notification(
        "테스트 알림", userId, ResourceType.COMMENT, UUID.randomUUID(), confirmed);
    ReflectionTestUtils.setField(notification, "createdAt", timestamp);
    ReflectionTestUtils.setField(notification, "updatedAt", timestamp);
    entityManager.persist(notification);
    entityManager.flush();
    entityManager.getEntityManager().createNativeQuery("""
            update notifications
            set created_at = :timestamp, updated_at = :timestamp
            where id = :id
            """)
        .setParameter("timestamp", timestamp)
        .setParameter("id", notification.getId())
        .executeUpdate();
    return notification;
  }

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }
}
