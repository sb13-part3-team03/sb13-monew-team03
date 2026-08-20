package com.codeit.monew.interest.service;

import com.codeit.monew.global.exception.UserNotFoundException;
import com.codeit.monew.interest.dto.response.CursorPageResponseInterestDto;
import com.codeit.monew.interest.dto.response.InterestDto;
import com.codeit.monew.interest.dto.response.SubscriptionDto;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Subscription;
import com.codeit.monew.interest.exception.AlreadySubscribedException;
import com.codeit.monew.interest.exception.InterestNotFoundException;
import com.codeit.monew.interest.exception.SubscriptionNotFoundException;
import com.codeit.monew.interest.repository.InterestRepository;
import com.codeit.monew.interest.repository.SubscriptionRepository;
import com.codeit.monew.interest.repository.projection.InterestSearchResult;
import com.codeit.monew.interest.service.command.*;
import com.codeit.monew.interest.service.condition.InterestSearchCondition;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterestService {

    private final InterestRepository interestRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Transactional
    public InterestDto createInterest(InterestRegisterCommand command) {
        /* Todo 80% 유사도 검증 로직

        */

        // Interest 생성
        Interest interest = new Interest(command.name(), command.keywords());

        // Interest 저장
        Interest savedInterest = interestRepository.save(interest);

        return InterestDto.from(savedInterest, 0L, false);
    }

    @Transactional
    public SubscriptionDto subscribe(InterestSubscribeCommand command) {
        // Interest 조회
        Interest interest = interestRepository.findById(command.interestId())
                .orElseThrow(() -> new InterestNotFoundException());

        // 이미 구독되어 있는 상태에서 구독 요청이 왔는지 검증
        validateDuplicateSubscription(command.userId(), command.interestId());

        // User 조회
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException());

        // Subscription 생성
        Subscription subscription = new Subscription(user, interest);

        // Subscription 저장
        Subscription savedSubscription = saveSubscription(subscription);

        // 구독자 수
        long subscriberCount = subscriptionRepository.countByInterestId(interest.getId());

        return SubscriptionDto.from(savedSubscription, subscriberCount);
    }

    public CursorPageResponseInterestDto findInterests(InterestSearchCondition condition) {
        int limit = condition.limit();

        List<InterestSearchResult> results = interestRepository.search(condition, limit + 1);

        boolean hasNext = results.size() > limit;

        if (hasNext) {
            results = results.subList(0, limit);
        }

        List<InterestDto> content = results.stream()
                .map(result -> InterestDto.from(
                        result.interest(),
                        result.subscriberCount(),
                        result.subscribedByMe()
                ))
                .toList();

        long totalElements = interestRepository.countByCondition(condition);

        String nextCursor = null;
        Instant nextAfter = null;

        if (hasNext && !results.isEmpty()) {
            InterestSearchResult last = results.get(results.size() - 1);

            nextCursor = "subscriberCount".equals(condition.orderBy())
                    ? String.valueOf(last.subscriberCount())
                    : last.interest().getName();

            nextAfter = last.interest().getCreatedAt();
        }

        return new CursorPageResponseInterestDto(
                content,
                nextCursor,
                nextAfter,
                content.size(),
                totalElements,
                hasNext
        );
    }

    @Transactional
    public InterestDto updateInterest(InterestUpdateCommand command) {
        // interest 조회
        Interest interest = interestRepository.findById(command.interestId())
                .orElseThrow(() -> new InterestNotFoundException());

        // interest 수정
        interest.updateKeywords(command.keywords());

        // 구독자 수
        long subscriberCount = subscriptionRepository.countByInterestId(interest.getId());

        return InterestDto.from(interest, subscriberCount, null);
    }

    @Transactional
    public void deleteInterest(InterestDeleteCommand command) {
        // Interest 조회
        Interest interest = interestRepository.findById(command.interestId())
                .orElseThrow(() -> new InterestNotFoundException());

        // 해당 Interest 구독되어 있던 정보 물리 삭제
        subscriptionRepository.deleteAllByInterestId(interest.getId());

        // Interest 물리 삭제
        interestRepository.delete(interest);
    }

    @Transactional
    public void unsubscribe(InterestUnsubscribeCommand command) {
        // Subscription 조회
        Subscription subscription = subscriptionRepository.findByUserIdAndInterestId(command.userId(), command.interestId())
                .orElseThrow(() -> new SubscriptionNotFoundException());

        // Subscription 물리 삭제
        subscriptionRepository.delete(subscription);
    }

    // 구독 저장 메서드
    private Subscription saveSubscription(Subscription subscription) {
        try {
            return subscriptionRepository.saveAndFlush(subscription);
        } catch (DataIntegrityViolationException e) {
            if (isDuplicateSubscriptionConstraint(e)) {
                throw new AlreadySubscribedException();
            }
            throw e;
        }
    }

    //DB의 UNIQUE 제약 판별
    private boolean isDuplicateSubscriptionConstraint(
            DataIntegrityViolationException e
    ) {
        Throwable cause = e;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException cve) {
                return "uk_subscription_user_interest"
                        .equals(cve.getConstraintName());
            }
            cause = cause.getCause();
        }

        return false;
    }

    // 이미 구독되어 있는 상태에서 구독 요청이 왔는지 검증
    private void validateDuplicateSubscription(UUID userId, UUID interestId) {
        if (subscriptionRepository.existsByUserIdAndInterestId(userId, interestId)) {
            throw new AlreadySubscribedException();
        }
    }
}
