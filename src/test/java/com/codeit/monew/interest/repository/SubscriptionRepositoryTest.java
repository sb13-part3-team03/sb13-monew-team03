package com.codeit.monew.interest.repository;

import com.codeit.monew.global.config.JpaAuditingConfig;
import com.codeit.monew.global.config.QuerydslConfig;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Subscription;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        QuerydslConfig.class,
        JpaAuditingConfig.class
})
class SubscriptionRepositoryTest {
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Nested
    @DisplayName("관심사 구독자 수 조회")
    class CountByInterestId {

        @Test
        @DisplayName("해당 관심사의 구독자 수를 반환한다")
        void success() {
            // given
            Interest interest = interestRepository.save(
                    new Interest("스포츠", List.of("축구", "야구"))
            );

            User user1 = userRepository.save(
                    new User(
                            "user1@test.com",
                            "user1",
                            "password"
                    )
            );

            User user2 = userRepository.save(
                    new User(
                            "user2@test.com",
                            "user2",
                            "password"
                    )
            );

            subscriptionRepository.save(new Subscription(user1, interest));
            subscriptionRepository.save(new Subscription(user2, interest));

            // when
            long count = subscriptionRepository.countByInterestId(
                    interest.getId()
            );

            // then
            assertThat(count).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("사용자 관심사 구독 여부 확인")
    class ExistsByUserIdAndInterestId {

        @Test
        @DisplayName("구독 중이면 true를 반환한다")
        void returnTrue_whenSubscribed() {
            // given
            Interest interest = interestRepository.save(
                    new Interest("스포츠", List.of("축구"))
            );

            User user = userRepository.save(
                    new User(
                            "user@test.com",
                            "user",
                            "password"
                    )
            );

            subscriptionRepository.save(
                    new Subscription(user, interest)
            );

            // when
            boolean exists =
                    subscriptionRepository.existsByUserIdAndInterestId(
                            user.getId(),
                            interest.getId()
                    );

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("구독하지 않았으면 false를 반환한다")
        void returnFalse_whenNotSubscribed() {
            // given
            Interest interest = interestRepository.save(
                    new Interest("스포츠", List.of("축구"))
            );

            User user = userRepository.save(
                    new User(
                            "user@test.com",
                            "user",
                            "password"
                    )
            );

            // when
            boolean exists =
                    subscriptionRepository.existsByUserIdAndInterestId(
                            user.getId(),
                            interest.getId()
                    );

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("사용자 관심사 구독 조회")
    class FindByUserIdAndInterestId {

        @Test
        @DisplayName("구독 정보가 존재하면 조회한다")
        void success() {
            // given
            Interest interest = interestRepository.save(
                    new Interest("스포츠", List.of("축구"))
            );

            User user = userRepository.save(
                    new User(
                            "user@test.com",
                            "user",
                            "password"
                    )
            );

            Subscription savedSubscription =
                    subscriptionRepository.save(
                            new Subscription(user, interest)
                    );

            // when
            Optional<Subscription> result =
                    subscriptionRepository.findByUserIdAndInterestId(
                            user.getId(),
                            interest.getId()
                    );

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId())
                    .isEqualTo(savedSubscription.getId());
        }

        @Test
        @DisplayName("구독 정보가 없으면 Optional.empty를 반환한다")
        void returnEmpty_whenNotFound() {
            // given
            Interest interest = interestRepository.save(
                    new Interest("스포츠", List.of("축구"))
            );

            User user = userRepository.save(
                    new User(
                            "user@test.com",
                            "user",
                            "password"
                    )
            );

            // when
            Optional<Subscription> result =
                    subscriptionRepository.findByUserIdAndInterestId(
                            user.getId(),
                            interest.getId()
                    );

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("관심사별 구독 조회")
    class FindAllByInterestId {

        @Test
        @DisplayName("관심사 ID로 해당 관심사의 구독 목록을 조회한다")
        void success() {
            // given
            User user1 = new User(
                    "user1@test.com",
                    "사용자1",
                    "password"
            );
            User user2 = new User(
                    "user2@test.com",
                    "사용자2",
                    "password"
            );

            userRepository.saveAllAndFlush(List.of(user1, user2));

            Interest interest1 = new Interest(
                    "스포츠",
                    List.of("축구", "야구")
            );
            Interest interest2 = new Interest(
                    "경제",
                    List.of("주식", "금리")
            );

            interestRepository.saveAllAndFlush(List.of(interest1, interest2));

            Subscription subscription1 =
                    new Subscription(user1, interest1);
            Subscription subscription2 =
                    new Subscription(user2, interest1);
            Subscription otherSubscription =
                    new Subscription(user1, interest2);

            subscriptionRepository.saveAllAndFlush(
                    List.of(subscription1, subscription2, otherSubscription)
            );

            // when
            List<Subscription> result =
                    subscriptionRepository.findAllByInterestId(interest1.getId());

            // then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(Subscription::getId)
                    .containsExactlyInAnyOrder(
                            subscription1.getId(),
                            subscription2.getId()
                    );
        }
    }

    @Nested
    @DisplayName("관심사 구독 전체 삭제")
    class DeleteAllSubscriptions {

        @Test
        @DisplayName("관심사 ID로 모든 구독 정보를 삭제한다")
        void success() {
            // given
            Interest interest = interestRepository.save(
                    new Interest("스포츠", List.of("축구"))
            );

            User user1 = userRepository.save(
                    new User(
                            "user1@test.com",
                            "user1",
                            "password"
                    )
            );

            User user2 = userRepository.save(
                    new User(
                            "user2@test.com",
                            "user2",
                            "password"
                    )
            );

            subscriptionRepository.save(
                    new Subscription(user1, interest)
            );
            subscriptionRepository.save(
                    new Subscription(user2, interest)
            );

            // when
            subscriptionRepository.deleteAllByInterestId(
                    interest.getId()
            );

            // then
            assertThat(
                    subscriptionRepository.countByInterestId(
                            interest.getId()
                    )
            ).isZero();
        }

        @Test
        @DisplayName("사용자 ID로 모든 구독 정보를 삭제한다")
        void deleteAllByUserId_success() {
            // given
            User user1 = userRepository.saveAndFlush(
                    new User(
                            "user1@test.com",
                            "사용자1",
                            "password"
                    )
            );

            User user2 = userRepository.saveAndFlush(
                    new User(
                            "user2@test.com",
                            "사용자2",
                            "password"
                    )
            );

            Interest interest1 = interestRepository.saveAndFlush(
                    new Interest(
                            "스포츠",
                            List.of("축구", "야구")
                    )
            );

            Interest interest2 = interestRepository.saveAndFlush(
                    new Interest(
                            "게임",
                            List.of("RPG", "FPS")
                    )
            );

            subscriptionRepository.saveAndFlush(
                    new Subscription(user1, interest1)
            );

            subscriptionRepository.saveAndFlush(
                    new Subscription(user1, interest2)
            );

            subscriptionRepository.saveAndFlush(
                    new Subscription(user2, interest1)
            );

            // when
            subscriptionRepository.deleteAllByUserId(user1.getId());
            subscriptionRepository.flush();

            // then
            List<Subscription> subscriptions =
                    subscriptionRepository.findAll();

            assertThat(subscriptions).hasSize(1);
            assertThat(subscriptions.get(0).getUser().getId())
                    .isEqualTo(user2.getId());
        }
    }

    @Nested
    @DisplayName("사용자 구독 목록 조회")
    class FindAllByUserIdOrderByCreatedAtDescIdDesc {

        @Test
        @DisplayName("사용자의 구독 목록을 생성일과 ID 기준 내림차순으로 조회한다")
        void success() {
            // given
            User user = userRepository.saveAndFlush(
                    new User(
                            "user@test.com",
                            "사용자",
                            "password"
                    )
            );

            Interest interest1 = new Interest(
                    "스포츠",
                    List.of("축구", "야구")
            );
            Interest interest2 = new Interest(
                    "경제",
                    List.of("주식", "금리")
            );
            Interest interest3 = new Interest(
                    "게임",
                    List.of("RPG", "FPS")
            );

            interestRepository.saveAllAndFlush(
                    List.of(interest1, interest2, interest3)
            );

            Subscription subscription1 =
                    new Subscription(user, interest1);
            Subscription subscription2 =
                    new Subscription(user, interest2);
            Subscription subscription3 =
                    new Subscription(user, interest3);

            subscriptionRepository.saveAllAndFlush(
                    List.of(subscription1, subscription2, subscription3)
            );

            Instant older = Instant.parse("2026-08-26T10:00:00Z");
            Instant newer = Instant.parse("2026-08-27T10:00:00Z");

            ReflectionTestUtils.setField(
                    subscription1,
                    "createdAt",
                    older
            );

            ReflectionTestUtils.setField(
                    subscription2,
                    "createdAt",
                    newer
            );

            ReflectionTestUtils.setField(
                    subscription3,
                    "createdAt",
                    newer
            );

            subscriptionRepository.flush();
            entityManager.clear();

            // when
            List<Subscription> result =
                    subscriptionRepository
                            .findAllByUserIdOrderByCreatedAtDescIdDesc(
                                    user.getId()
                            );

            // then
            assertThat(result).hasSize(3);

            List<UUID> sameCreatedAtIds = List.of(
                            subscription2.getId(),
                            subscription3.getId()
                    ).stream()
                    .sorted(
                            Comparator.comparing(UUID::toString)
                                    .reversed()
                    )
                    .toList();

            assertThat(result)
                    .extracting(Subscription::getId)
                    .containsExactly(
                            sameCreatedAtIds.get(0),
                            sameCreatedAtIds.get(1),
                            subscription1.getId()
                    );

            assertThat(result)
                    .allSatisfy(subscription -> {
                        assertThat(
                                Hibernate.isInitialized(
                                        subscription.getInterest()
                                )
                        ).isTrue();

                        assertThat(
                                Hibernate.isInitialized(
                                        subscription.getInterest().getKeywords()
                                )
                        ).isTrue();
                    });
        }
    }
}