package com.codeit.monew.notification.condition;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeit.monew.global.exception.MonewException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("알림 검색 조건 테스트")
class NotificationSearchConditionTest {

  private final UUID userId = UUID.randomUUID();
  private final String cursor = UUID.randomUUID().toString();
  private final Instant after = Instant.parse("2026-09-01T00:00:00Z");

  @Test
  @DisplayName("커서와 기준 시간이 모두 없으면 첫 페이지 조건으로 생성")
  void createFirstPageCondition() {
    assertThatCode(() -> new NotificationSearchCondition(userId, null, null, 10))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("커서와 기준 시간이 모두 있으면 다음 페이지 조건으로 생성")
  void createNextPageCondition() {
    assertThatCode(() -> new NotificationSearchCondition(userId, cursor, after, 10))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("커서만 제공하면 유효하지 않은 요청")
  void createWithOnlyCursor() {
    assertThatThrownBy(() -> new NotificationSearchCondition(userId, cursor, null, 10))
        .isInstanceOf(MonewException.class);
  }

  @Test
  @DisplayName("기준 시간만 제공하면 유효하지 않은 요청")
  void createWithOnlyAfter() {
    assertThatThrownBy(() -> new NotificationSearchCondition(userId, null, after, 10))
        .isInstanceOf(MonewException.class);
  }
}
