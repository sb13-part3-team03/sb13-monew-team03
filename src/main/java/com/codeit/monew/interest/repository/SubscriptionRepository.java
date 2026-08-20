package com.codeit.monew.interest.repository;

import com.codeit.monew.interest.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    long countByInterestId(UUID interestId);
    void deleteAllByInterestId(UUID interestId);
    boolean existsByUserIdAndInterestId(UUID userId, UUID interestId);
    Optional<Subscription> findByUserIdAndInterestId(UUID userId, UUID interestId);

}
