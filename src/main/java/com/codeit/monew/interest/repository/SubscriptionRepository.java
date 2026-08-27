package com.codeit.monew.interest.repository;

import com.codeit.monew.interest.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    @EntityGraph(attributePaths = {"interest", "interest.keywords"})
    List<Subscription> findAllByUserIdOrderByCreatedAtDescIdDesc(UUID userId);

    long countByInterestId(UUID interestId);
    void deleteAllByInterestId(UUID interestId);
    boolean existsByUserIdAndInterestId(UUID userId, UUID interestId);
    Optional<Subscription> findByUserIdAndInterestId(UUID userId, UUID interestId);
    List<Subscription> findAllByInterestId(UUID interestId);
    void deleteAllByUserId(UUID userId);

}
