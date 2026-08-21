package com.codeit.monew.interest.service;

import com.codeit.monew.interest.dto.response.CursorPageResponseInterestDto;
import com.codeit.monew.interest.dto.response.InterestDto;
import com.codeit.monew.interest.dto.response.SubscriptionDto;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Subscription;
import com.codeit.monew.interest.exception.AlreadySubscribedException;
import com.codeit.monew.interest.exception.InterestNotFoundException;
import com.codeit.monew.interest.exception.SimilarInterestNameException;
import com.codeit.monew.interest.exception.SubscriptionNotFoundException;
import com.codeit.monew.interest.repository.InterestRepository;
import com.codeit.monew.interest.repository.SubscriptionRepository;
import com.codeit.monew.interest.repository.projection.InterestSearchResult;
import com.codeit.monew.interest.service.command.*;
import com.codeit.monew.interest.service.condition.InterestSearchCondition;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {
    @Mock
    private InterestRepository interestRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InterestService interestService;

    @Nested
    @DisplayName("관심사 등록")
    class CreateInterest {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            InterestRegisterCommand command = new InterestRegisterCommand(
                    "스포츠",
                    List.of("축구", "야구")
            );

            Interest savedInterest = new Interest(
                    command.name(),
                    command.keywords()
            );

            given(interestRepository.findAll())
                    .willReturn(List.of());

            given(interestRepository.save(any(Interest.class)))
                    .willReturn(savedInterest);

            // when
            InterestDto result = interestService.createInterest(command);

            // then
            assertThat(result.name()).isEqualTo("스포츠");
            assertThat(result.keywords()).containsExactly("축구", "야구");
            assertThat(result.subscriberCount()).isZero();
            assertThat(result.subscribedByMe()).isFalse();

            then(interestRepository)
                    .should()
                    .save(any(Interest.class));
        }

        @Test
        @DisplayName("80% 이상 유사한 이름의 관심사가 존재하면 실패")
        void fail_whenSimilarInterestNameExists() {
            // given
            InterestRegisterCommand command = new InterestRegisterCommand(
                    "스포츠뉴스",
                    List.of("축구")
            );

            Interest existingInterest = new Interest(
                    "스포츠뉴수",
                    List.of("야구")
            );

            given(interestRepository.findAll())
                    .willReturn(List.of(existingInterest));

            // when & then
            assertThatThrownBy(
                    () -> interestService.createInterest(command)
            ).isInstanceOf(SimilarInterestNameException.class);

            then(interestRepository)
                    .should(never())
                    .save(any(Interest.class));
        }

        @Test
        @DisplayName("동일한 이름의 관심사가 존재하면 실패")
        void fail_whenSameInterestNameExists() {
            // given
            InterestRegisterCommand command = new InterestRegisterCommand(
                    "스포츠",
                    List.of("축구")
            );

            Interest existingInterest = new Interest(
                    "스포츠",
                    List.of("야구")
            );

            given(interestRepository.findAll())
                    .willReturn(List.of(existingInterest));

            // when & then
            assertThatThrownBy(
                    () -> interestService.createInterest(command)
            ).isInstanceOf(SimilarInterestNameException.class);

            then(interestRepository)
                    .should(never())
                    .save(any(Interest.class));
        }

        @Test
        @DisplayName("80% 미만으로 유사한 이름은 등록 성공")
        void success_whenSimilarityIsLessThan80Percent() {
            // given
            InterestRegisterCommand command = new InterestRegisterCommand(
                    "스포츠",
                    List.of("축구", "야구")
            );

            Interest existingInterest = new Interest(
                    "스포츠1",
                    List.of("농구")
            );

            Interest savedInterest = new Interest(
                    command.name(),
                    command.keywords()
            );

            given(interestRepository.findAll())
                    .willReturn(List.of(existingInterest));

            given(interestRepository.save(any(Interest.class)))
                    .willReturn(savedInterest);

            // when
            InterestDto result =
                    interestService.createInterest(command);

            // then
            assertThat(result.name()).isEqualTo("스포츠");

            then(interestRepository)
                    .should()
                    .save(any(Interest.class));
        }
    }

    @Nested
    @DisplayName("관심사 구독")
    class Subscribe {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            UUID interestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Interest interest = new Interest(
                    "스포츠",
                    List.of("축구", "야구")
            );

            User user = new User(
                    "test@test.com",
                    "테스트",
                    "password"
            );

            InterestSubscribeCommand command =
                    new InterestSubscribeCommand(
                            interestId,
                            userId
                    );

            given(interestRepository.findById(interestId))
                    .willReturn(Optional.of(interest));

            given(subscriptionRepository.existsByUserIdAndInterestId(
                    userId,
                    interestId
            )).willReturn(false);

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(subscriptionRepository.saveAndFlush(any(Subscription.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            given(subscriptionRepository.countByInterestId(interest.getId()))
                    .willReturn(1L);

            // when
            SubscriptionDto result =
                    interestService.subscribe(command);

            // then
            assertThat(result.interestName())
                    .isEqualTo("스포츠");

            assertThat(result.interestKeywords())
                    .containsExactly("축구", "야구");

            assertThat(result.interestSubscriberCount())
                    .isEqualTo(1L);

            then(subscriptionRepository)
                    .should()
                    .saveAndFlush(any(Subscription.class));
        }

        @Test
        @DisplayName("이미 구독 중이면 실패")
        void fail_whenAlreadySubscribed() {
            // given
            UUID interestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Interest interest = new Interest(
                    "스포츠",
                    List.of("축구")
            );

            InterestSubscribeCommand command =
                    new InterestSubscribeCommand(
                            interestId,
                            userId
                    );

            given(interestRepository.findById(interestId))
                    .willReturn(Optional.of(interest));

            given(subscriptionRepository.existsByUserIdAndInterestId(
                    userId,
                    interestId
            )).willReturn(true);

            // when & then
            assertThatThrownBy(() -> interestService.subscribe(command))
                    .isInstanceOf(AlreadySubscribedException.class);

            then(userRepository)
                    .shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("관심사가 존재하지 않으면 실패")
        void fail_whenInterestNotFound() {
            // given
            UUID interestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(interestRepository.findById(interestId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> interestService.subscribe(
                            new InterestSubscribeCommand(
                                    interestId,
                                    userId
                            )
                    )
            ).isInstanceOf(InterestNotFoundException.class);

            then(subscriptionRepository)
                    .shouldHaveNoInteractions();

            then(userRepository)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("관심사 목록 조회")
    class FindInterests {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            UUID userId = UUID.randomUUID();

            InterestSearchCondition condition = new InterestSearchCondition(
                    "스포츠",
                    "name",
                    "ASC",
                    null,
                    null,
                    2,
                    userId
            );

            Interest interest1 = new Interest(
                    "스포츠",
                    List.of("축구", "야구")
            );

            Interest interest2 = new Interest(
                    "스포츠 뉴스",
                    List.of("농구", "배구")
            );

            given(interestRepository.search(condition, 3))
                    .willReturn(List.of(
                            new InterestSearchResult(interest1, 3L, false),
                            new InterestSearchResult(interest2, 5L, false)
                    ));

            given(interestRepository.countByCondition(condition))
                    .willReturn(2L);

            // when
            CursorPageResponseInterestDto result =
                    interestService.findInterests(condition);

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.totalElements()).isEqualTo(2L);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.nextCursor()).isNull();
            assertThat(result.nextAfter()).isNull();

            assertThat(result.content().get(0).subscriberCount())
                    .isEqualTo(3L);

            assertThat(result.content().get(1).subscriberCount())
                    .isEqualTo(5L);

            then(interestRepository)
                    .should()
                    .search(condition, 3);
        }

        @Test
        @DisplayName("다음 페이지가 있으면 커서 정보를 반환")
        void success_withNextPage() {
            // given
            UUID userId = UUID.randomUUID();

            InterestSearchCondition condition = new InterestSearchCondition(
                    null,
                    "name",
                    "ASC",
                    null,
                    null,
                    2,
                    userId
            );

            Interest interest1 = new Interest(
                    "게임",
                    List.of("PC", "콘솔")
            );

            Interest interest2 = new Interest(
                    "스포츠",
                    List.of("축구", "야구")
            );

            Interest interest3 = new Interest(
                    "여행",
                    List.of("국내", "해외")
            );

            Instant createdAt =
                    Instant.parse("2026-08-20T10:00:00Z");

            ReflectionTestUtils.setField(
                    interest2,
                    "createdAt",
                    createdAt
            );

            given(interestRepository.search(condition, 3))
                    .willReturn(List.of(
                            new InterestSearchResult(interest1, 3L, false),
                            new InterestSearchResult(interest2, 5L, false),
                            new InterestSearchResult(interest3, 7L, false)
                    ));

            given(interestRepository.countByCondition(condition))
                    .willReturn(3L);

            // when
            CursorPageResponseInterestDto result =
                    interestService.findInterests(condition);

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.hasNext()).isTrue();

            assertThat(result.nextCursor())
                    .isEqualTo("스포츠");

            assertThat(result.nextAfter())
                    .isEqualTo(createdAt);
        }

        @Test
        @DisplayName("구독한 관심사는 subscribedByMe가 true")
        void success_whenSubscribed() {
            // given
            UUID userId = UUID.randomUUID();

            InterestSearchCondition condition = new InterestSearchCondition(
                    null,
                    "name",
                    "ASC",
                    null,
                    null,
                    2,
                    userId
            );

            Interest subscribedInterest = new Interest(
                    "스포츠",
                    List.of("축구", "야구")
            );

            Interest notSubscribedInterest = new Interest(
                    "여행",
                    List.of("국내", "해외")
            );

            given(interestRepository.search(condition, 3))
                    .willReturn(List.of(
                            new InterestSearchResult(
                                    subscribedInterest,
                                    10L,
                                    true
                            ),
                            new InterestSearchResult(
                                    notSubscribedInterest,
                                    5L,
                                    false
                            )
                    ));

            given(interestRepository.countByCondition(condition))
                    .willReturn(2L);

            // when
            CursorPageResponseInterestDto result =
                    interestService.findInterests(condition);

            // then
            assertThat(result.content()).hasSize(2);

            assertThat(result.content().get(0).subscribedByMe())
                    .isTrue();

            assertThat(result.content().get(1).subscribedByMe())
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("관심사 수정")
    class UpdateInterest {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            UUID interestId = UUID.randomUUID();

            Interest interest = new Interest(
                    "스포츠",
                    List.of("축구", "야구")
            );

            InterestUpdateCommand command = new InterestUpdateCommand(
                    interestId,
                    List.of("농구", "배구")
            );

            given(interestRepository.findById(interestId))
                    .willReturn(Optional.of(interest));

            given(subscriptionRepository.countByInterestId(interest.getId()))
                    .willReturn(3L);

            // when
            InterestDto result = interestService.updateInterest(command);

            // then
            assertThat(interest.getKeywords())
                    .containsExactly("농구", "배구");

            assertThat(result.keywords())
                    .containsExactly("농구", "배구");

            assertThat(result.subscriberCount())
                    .isEqualTo(3L);

            assertThat(result.subscribedByMe())
                    .isNull();

            then(interestRepository)
                    .should()
                    .findById(interestId);
        }

        @Test
        @DisplayName("관심사가 존재하지 않으면 실패")
        void fail_whenInterestNotFound() {
            // given
            UUID interestId = UUID.randomUUID();

            InterestUpdateCommand command = new InterestUpdateCommand(
                    interestId,
                    List.of("농구")
            );

            given(interestRepository.findById(interestId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interestService.updateInterest(command))
                    .isInstanceOf(InterestNotFoundException.class);

            then(subscriptionRepository)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("관심사 삭제")
    class DeleteInterest {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            UUID interestId = UUID.randomUUID();

            Interest interest = new Interest(
                    "스포츠",
                    List.of("축구")
            );

            given(interestRepository.findById(interestId))
                    .willReturn(Optional.of(interest));

            // when
            interestService.deleteInterest(
                    new InterestDeleteCommand(interestId)
            );

            // then
            then(subscriptionRepository)
                    .should()
                    .deleteAllByInterestId(interest.getId());

            then(interestRepository)
                    .should()
                    .delete(interest);
        }

        @Test
        @DisplayName("관심사가 존재하지 않으면 실패")
        void fail_whenInterestNotFound() {
            // given
            UUID interestId = UUID.randomUUID();

            given(interestRepository.findById(interestId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> interestService.deleteInterest(
                            new InterestDeleteCommand(interestId)
                    )
            ).isInstanceOf(InterestNotFoundException.class);

            then(subscriptionRepository)
                    .shouldHaveNoInteractions();
        }
    }



    @Nested
    @DisplayName("관심사 구독 취소")
    class Unsubscribe {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            UUID interestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            User user = new User(
                    "test@test.com",
                    "테스트",
                    "password"
            );

            Interest interest = new Interest(
                    "스포츠",
                    List.of("축구")
            );

            Subscription subscription =
                    new Subscription(user, interest);

            given(subscriptionRepository.findByUserIdAndInterestId(
                    userId,
                    interestId
            )).willReturn(Optional.of(subscription));

            // when
            interestService.unsubscribe(
                    new InterestUnsubscribeCommand(
                            interestId,
                            userId
                    )
            );

            // then
            then(subscriptionRepository)
                    .should()
                    .delete(subscription);
        }

        @Test
        @DisplayName("구독 정보가 존재하지 않으면 실패")
        void fail_whenSubscriptionNotFound() {
            // given
            UUID interestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(subscriptionRepository.findByUserIdAndInterestId(
                    userId,
                    interestId
            )).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> interestService.unsubscribe(
                            new InterestUnsubscribeCommand(
                                    interestId,
                                    userId
                            )
                    )
            ).isInstanceOf(SubscriptionNotFoundException.class);

            then(subscriptionRepository)
                    .should()
                    .findByUserIdAndInterestId(
                            userId,
                            interestId
                    );
        }
    }
}