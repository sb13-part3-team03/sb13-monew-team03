package com.codeit.monew.interest.repository;

import com.codeit.monew.global.config.JpaAuditingConfig;
import com.codeit.monew.global.config.QuerydslConfig;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Subscription;
import com.codeit.monew.interest.repository.projection.InterestSearchResult;
import com.codeit.monew.interest.service.condition.InterestSearchCondition;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        QuerydslConfig.class,
        JpaAuditingConfig.class
})
class InterestRepositoryTest {
    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Nested
    @DisplayName("관심사 목록 조회")
    class Search {

        @Test
        @DisplayName("관심사 이름으로 부분일치 검색 성공")
        void success_whenNameContainsKeyword() {
            // given
            Interest sports = saveInterest(
                    "스포츠",
                    List.of("축구", "야구")
            );

            saveInterest(
                    "게임",
                    List.of("RPG", "FPS")
            );

            InterestSearchCondition condition =
                    createCondition(
                            "포츠",
                            "name",
                            "ASC",
                            null,
                            null,
                            10,
                            UUID.randomUUID()
                    );

            // when
            List<InterestSearchResult> result =
                    interestRepository.search(condition, 10);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).interest().getId())
                    .isEqualTo(sports.getId());
        }

        @Test
        @DisplayName("관심사 키워드로 부분일치 검색 성공")
        void success_whenInterestKeywordContainsKeyword() {
            // given
            Interest sports = saveInterest(
                    "스포츠",
                    List.of("축구", "야구")
            );

            saveInterest(
                    "게임",
                    List.of("RPG", "FPS")
            );

            InterestSearchCondition condition =
                    createCondition(
                            "축구",
                            "name",
                            "ASC",
                            null,
                            null,
                            10,
                            UUID.randomUUID()
                    );

            // when
            List<InterestSearchResult> result =
                    interestRepository.search(condition, 10);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).interest().getId())
                    .isEqualTo(sports.getId());
        }

        @Test
        @DisplayName("이름 오름차순 정렬 성공")
        void success_sortByNameAsc() {
            // given
            saveInterest("여행", List.of("해외"));
            saveInterest("스포츠", List.of("축구"));
            saveInterest("게임", List.of("RPG"));

            InterestSearchCondition condition =
                    createCondition(
                            null,
                            "name",
                            "ASC",
                            null,
                            null,
                            10,
                            UUID.randomUUID()
                    );

            // when
            List<InterestSearchResult> result =
                    interestRepository.search(condition, 10);

            // then
            assertThat(result)
                    .extracting(searchResult ->
                            searchResult.interest().getName())
                    .containsExactly(
                            "게임",
                            "스포츠",
                            "여행"
                    );
        }

        @Test
        @DisplayName("이름 내림차순 정렬 성공")
        void success_sortByNameDesc() {
            // given
            saveInterest("여행", List.of("해외"));
            saveInterest("스포츠", List.of("축구"));
            saveInterest("게임", List.of("RPG"));

            InterestSearchCondition condition =
                    createCondition(
                            null,
                            "name",
                            "DESC",
                            null,
                            null,
                            10,
                            UUID.randomUUID()
                    );

            // when
            List<InterestSearchResult> result =
                    interestRepository.search(condition, 10);

            // then
            assertThat(result)
                    .extracting(searchResult ->
                            searchResult.interest().getName())
                    .containsExactly(
                            "여행",
                            "스포츠",
                            "게임"
                    );
        }

        @Test
        @DisplayName("구독자 수 내림차순 정렬 성공")
        void success_sortBySubscriberCountDesc() {
            // given
            Interest sports =
                    saveInterest("스포츠", List.of("축구"));

            Interest game =
                    saveInterest("게임", List.of("RPG"));

            Interest travel =
                    saveInterest("여행", List.of("해외"));

            User user1 = saveUser(
                    "user1@test.com",
                    "사용자1"
            );
            User user2 = saveUser(
                    "user2@test.com",
                    "사용자2"
            );
            User user3 = saveUser(
                    "user3@test.com",
                    "사용자3"
            );

            subscribe(user1, sports);
            subscribe(user2, sports);
            subscribe(user3, sports);

            subscribe(user1, game);
            subscribe(user2, game);

            subscribe(user1, travel);

            InterestSearchCondition condition =
                    createCondition(
                            null,
                            "subscriberCount",
                            "DESC",
                            null,
                            null,
                            10,
                            user1.getId()
                    );

            // when
            List<InterestSearchResult> result =
                    interestRepository.search(condition, 10);

            // then
            assertThat(result)
                    .extracting(InterestSearchResult::subscriberCount)
                    .containsExactly(3L, 2L, 1L);

            assertThat(result)
                    .extracting(searchResult ->
                            searchResult.interest().getName())
                    .containsExactly(
                            "스포츠",
                            "게임",
                            "여행"
                    );
        }

        @Test
        @DisplayName("구독자 수 오름차순 정렬 성공")
        void success_sortBySubscriberCountAsc() {
            // given
            Interest sports =
                    saveInterest("스포츠", List.of("축구"));

            Interest game =
                    saveInterest("게임", List.of("RPG"));

            Interest travel =
                    saveInterest("여행", List.of("해외"));

            User user1 = saveUser(
                    "user1@test.com",
                    "사용자1"
            );
            User user2 = saveUser(
                    "user2@test.com",
                    "사용자2"
            );

            subscribe(user1, sports);
            subscribe(user2, sports);

            subscribe(user1, game);

            InterestSearchCondition condition =
                    createCondition(
                            null,
                            "subscriberCount",
                            "ASC",
                            null,
                            null,
                            10,
                            user1.getId()
                    );

            // when
            List<InterestSearchResult> result =
                    interestRepository.search(condition, 10);

            // then
            assertThat(result)
                    .extracting(InterestSearchResult::subscriberCount)
                    .containsExactly(0L, 1L, 2L);

            assertThat(result)
                    .extracting(searchResult ->
                            searchResult.interest().getName())
                    .containsExactly(
                            "여행",
                            "게임",
                            "스포츠"
                    );
        }

        @Test
        @DisplayName("이름 기준 커서 이후 데이터 조회 성공")
        void success_withNameCursor() {
            // given
            saveInterest("게임", List.of("RPG"));

            Interest sports =
                    saveInterest("스포츠", List.of("축구"));

            saveInterest("여행", List.of("해외"));

            UUID sportsId = sports.getId();

            entityManager.flush();
            entityManager.clear();

            Interest savedSports = interestRepository.findById(sportsId)
                    .orElseThrow();

            InterestSearchCondition condition =
                    createCondition(
                            null,
                            "name",
                            "ASC",
                            "스포츠",
                            savedSports.getCreatedAt(),
                            10,
                            UUID.randomUUID()
                    );

            // when
            List<InterestSearchResult> result =
                    interestRepository.search(condition, 10);

            // then
            assertThat(result)
                    .extracting(searchResult ->
                            searchResult.interest().getName())
                    .containsExactly("여행");
        }

        @Test
        @DisplayName("구독자 수 기준 커서 이후 데이터 조회 성공")
        void success_withSubscriberCountCursor() {
            // given
            Interest sports =
                    saveInterest("스포츠", List.of("축구"));

            Interest game =
                    saveInterest("게임", List.of("RPG"));

            Interest travel =
                    saveInterest("여행", List.of("해외"));

            User user1 = saveUser(
                    "user1@test.com",
                    "사용자1"
            );
            User user2 = saveUser(
                    "user2@test.com",
                    "사용자2"
            );
            User user3 = saveUser(
                    "user3@test.com",
                    "사용자3"
            );

            subscribe(user1, sports);
            subscribe(user2, sports);
            subscribe(user3, sports);

            subscribe(user1, game);
            subscribe(user2, game);

            subscribe(user1, travel);

            UUID gameId = game.getId();

            entityManager.flush();
            entityManager.clear();

            Interest savedGame = interestRepository.findById(gameId)
                    .orElseThrow();

            InterestSearchCondition condition =
                    createCondition(
                            null,
                            "subscriberCount",
                            "DESC",
                            "2",
                            savedGame.getCreatedAt(),
                            10,
                            user1.getId()
                    );

            // when
            List<InterestSearchResult> result =
                    interestRepository.search(condition, 10);

            // then
            assertThat(result).hasSize(1);

            assertThat(result.get(0).interest().getName())
                    .isEqualTo("여행");

            assertThat(result.get(0).subscriberCount())
                    .isEqualTo(1L);
        }

        @Test
        @DisplayName("구독자 수와 요청자의 구독 여부 조회 성공")
        void success_withSubscriberCountAndSubscribedByMe() {
            // given
            Interest sports =
                    saveInterest("스포츠", List.of("축구"));

            Interest game =
                    saveInterest("게임", List.of("RPG"));

            User requestUser = saveUser(
                    "request@test.com",
                    "요청자"
            );

            User otherUser = saveUser(
                    "other@test.com",
                    "다른사용자"
            );

            subscribe(requestUser, sports);
            subscribe(otherUser, sports);
            subscribe(otherUser, game);

            InterestSearchCondition condition =
                    createCondition(
                            null,
                            "name",
                            "ASC",
                            null,
                            null,
                            10,
                            requestUser.getId()
                    );

            // when
            List<InterestSearchResult> result =
                    interestRepository.search(condition, 10);

            // then
            InterestSearchResult sportsResult = result.stream()
                    .filter(searchResult ->
                            searchResult.interest()
                                    .getName()
                                    .equals("스포츠"))
                    .findFirst()
                    .orElseThrow();

            InterestSearchResult gameResult = result.stream()
                    .filter(searchResult ->
                            searchResult.interest()
                                    .getName()
                                    .equals("게임"))
                    .findFirst()
                    .orElseThrow();

            assertThat(sportsResult.subscriberCount())
                    .isEqualTo(2L);
            assertThat(sportsResult.subscribedByMe())
                    .isTrue();

            assertThat(gameResult.subscriberCount())
                    .isEqualTo(1L);
            assertThat(gameResult.subscribedByMe())
                    .isFalse();
        }

        @Test
        @DisplayName("검색 조건에 맞는 전체 관심사 수 조회 성공")
        void success_countByCondition() {
            // given
            saveInterest(
                    "스포츠",
                    List.of("축구", "야구")
            );

            saveInterest(
                    "축구 뉴스",
                    List.of("국가대표")
            );

            saveInterest(
                    "게임",
                    List.of("RPG")
            );

            InterestSearchCondition condition =
                    createCondition(
                            "축구",
                            "name",
                            "ASC",
                            null,
                            null,
                            10,
                            UUID.randomUUID()
                    );

            // when
            long result =
                    interestRepository.countByCondition(condition);

            // then
            assertThat(result).isEqualTo(2L);
        }

        @Test
        @DisplayName("이름 기준 커서만 있어도 이후 데이터 조회 성공")
        void success_withNameCursorWithoutAfter() {
            // given
            saveInterest("게임", List.of("RPG"));
            saveInterest("스포츠", List.of("축구"));
            saveInterest("여행", List.of("해외"));

            InterestSearchCondition condition =
                    createCondition(
                            null,
                            "name",
                            "ASC",
                            "스포츠",
                            null,
                            10,
                            UUID.randomUUID()
                    );

            // when
            List<InterestSearchResult> result =
                    interestRepository.search(condition, 10);

            // then
            assertThat(result)
                    .extracting(searchResult ->
                            searchResult.interest().getName())
                    .containsExactly("여행");
        }

        @Test
        @DisplayName("구독자 수 기준 커서만 있어도 이후 데이터 조회 성공")
        void success_withSubscriberCountCursorWithoutAfter() {
            // given
            Interest sports =
                    saveInterest("스포츠", List.of("축구"));

            Interest game =
                    saveInterest("게임", List.of("RPG"));

            Interest travel =
                    saveInterest("여행", List.of("해외"));

            User user1 = saveUser(
                    "user1@test.com",
                    "사용자1"
            );
            User user2 = saveUser(
                    "user2@test.com",
                    "사용자2"
            );
            User user3 = saveUser(
                    "user3@test.com",
                    "사용자3"
            );

            subscribe(user1, sports);
            subscribe(user2, sports);
            subscribe(user3, sports);

            subscribe(user1, game);
            subscribe(user2, game);

            subscribe(user1, travel);

            InterestSearchCondition condition =
                    createCondition(
                            null,
                            "subscriberCount",
                            "DESC",
                            "2",
                            null,
                            10,
                            user1.getId()
                    );

            // when
            List<InterestSearchResult> result =
                    interestRepository.search(condition, 10);

            // then
            assertThat(result).hasSize(1);

            assertThat(result.get(0).interest().getName())
                    .isEqualTo("여행");

            assertThat(result.get(0).subscriberCount())
                    .isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("관심사 키워드 포함 전체 조회")
    class FindAllWithKeywords {

        @Test
        @DisplayName("관심사와 키워드를 함께 조회한다")
        void success() {
            // given
            Interest interest1 = new Interest(
                    "스포츠",
                    List.of("축구", "야구")
            );

            Interest interest2 = new Interest(
                    "경제",
                    List.of("주식", "금리")
            );

            interestRepository.saveAll(List.of(interest1, interest2));

            entityManager.flush();
            entityManager.clear();

            // when
            List<Interest> result = interestRepository.findAllWithKeywords();

            // then
            assertThat(result).hasSize(2);

            Interest sports = result.stream()
                    .filter(interest -> interest.getName().equals("스포츠"))
                    .findFirst()
                    .orElseThrow();

            Interest economy = result.stream()
                    .filter(interest -> interest.getName().equals("경제"))
                    .findFirst()
                    .orElseThrow();

            assertThat(Hibernate.isInitialized(sports.getKeywords())).isTrue();
            assertThat(Hibernate.isInitialized(economy.getKeywords())).isTrue();

            assertThat(sports.getKeywords())
                    .containsExactlyInAnyOrder("축구", "야구");

            assertThat(economy.getKeywords())
                    .containsExactlyInAnyOrder("주식", "금리");
        }
    }

    private Interest saveInterest(
            String name,
            List<String> keywords
    ) {
        Interest interest =
                new Interest(name, keywords);

        Interest saved =
                interestRepository.saveAndFlush(interest);

        return saved;
    }

    private User saveUser(
            String email,
            String nickname
    ) {
        User user =
                new User(
                        email,
                        nickname,
                        "password"
                );

        return userRepository.saveAndFlush(user);
    }

    private void subscribe(
            User user,
            Interest interest
    ) {
        subscriptionRepository.saveAndFlush(
                new Subscription(user, interest)
        );
    }

    private InterestSearchCondition createCondition(
            String keyword,
            String orderBy,
            String direction,
            String cursor,
            java.time.Instant after,
            int limit,
            UUID userId
    ) {
        return new InterestSearchCondition(
                keyword,
                orderBy,
                direction,
                cursor,
                after,
                limit,
                userId
        );
    }
}