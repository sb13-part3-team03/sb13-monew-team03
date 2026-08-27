package com.codeit.monew.useractivity.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.repository.Query;

@DisplayName("활동내역 레포지토리 테스트")
class UserActivityRepositoryTest {

  @Test
  @DisplayName("삭제되지 않은 활동내역의 최근 항목을 10개씩 조회")
  void findById_excludesDeletedActivityAndLimitsRecentItems() throws Exception {
    Query query = UserActivityRepository.class.getMethod("findById", UUID.class)
        .getAnnotation(Query.class);

    assertThat(query.value()).contains("'deleted': { '$ne': true }");
    assertThat(query.fields())
        .contains("'comments': { '$slice': 10 }")
        .contains("'commentLikes': { '$slice': 10 }")
        .contains("'articleViews': { '$slice': 10 }");
  }
}
