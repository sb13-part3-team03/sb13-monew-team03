package com.codeit.monew.useractivity.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

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

}
