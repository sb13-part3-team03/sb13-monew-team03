package com.codeit.monew.useractivity.repository;

import com.codeit.monew.useractivity.entity.UserActivity;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserActivityRepository extends MongoRepository<UserActivity, UUID> {

  //저장된 배열은 유지하고 최신순으로 정렬된 각 목록의 앞 10개만 조회한다.
  //일부 기록만 포함한 조회 결과이므로 이 문서를 통째로 다시 저장하지 않는다.

  @Override
  @Query(value = "{ '_id': ?0 }", fields = """
      { 'comments': { '$slice': 10 },
        'commentLikes': { '$slice': 10 },
        'articleViews': { '$slice': 10 } }
      """)
  Optional<UserActivity> findById(UUID userId);
}
