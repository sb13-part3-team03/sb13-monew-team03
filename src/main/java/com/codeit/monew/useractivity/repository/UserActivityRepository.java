package com.codeit.monew.useractivity.repository;

import com.codeit.monew.useractivity.entity.UserActivity;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserActivityRepository extends MongoRepository<UserActivity, UUID> {
}
