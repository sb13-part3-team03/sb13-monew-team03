package com.codeit.monew.interest.service;

import com.codeit.monew.article.repository.ArticleInterestRepository;
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
import com.codeit.monew.user.exception.UserNotFoundException;
import com.codeit.monew.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterestService {

    private final InterestRepository interestRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ArticleInterestRepository articleInterestRepository;
    private final UserRepository userRepository;

    @Transactional
    public InterestDto createInterest(InterestRegisterCommand command) {
        // Levenshtein Distance를 사용한 80% 유사도 검증 로직
        validateSimilarName(command.name());

        // Interest 생성
        Interest interest = new Interest(command.name(), command.keywords());

        // Interest 저장
        Interest savedInterest = interestRepository.save(interest);

        log.info("Interest created: interestId={}", savedInterest.getId());

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

        log.info("Interest subscribed: interestId={}, userId={}", command.interestId(), command.userId());

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

        log.info("Interest updated: interestId={}", interest.getId());

        // 구독자 수
        long subscriberCount = subscriptionRepository.countByInterestId(interest.getId());

        return InterestDto.from(interest, subscriberCount, null);
    }

    @Transactional
    public void deleteInterest(InterestDeleteCommand command) {
        // Interest 조회
        Interest interest = interestRepository.findById(command.interestId())
                .orElseThrow(() -> new InterestNotFoundException());

        // Article과 Interest 연결 정보 물리 삭제
        articleInterestRepository.deleteAllByInterest_Id(interest.getId());

        // 해당 Interest 구독되어 있던 정보 물리 삭제
        subscriptionRepository.deleteAllByInterestId(interest.getId());

        // Interest 물리 삭제
        interestRepository.delete(interest);

        log.info("Interest deleted: interestId={}", interest.getId());
    }

    @Transactional
    public void unsubscribe(InterestUnsubscribeCommand command) {
        // Subscription 조회
        Subscription subscription = subscriptionRepository.findByUserIdAndInterestId(command.userId(), command.interestId())
                .orElseThrow(() -> new SubscriptionNotFoundException());

        // Subscription 물리 삭제
        subscriptionRepository.delete(subscription);

        log.info("Interest unsubscribed: interestId={}, userId={}", command.interestId(), command.userId());
    }

    // 관심사 이름 유사도 계산 및 검증
    private void validateSimilarName(String name) {
        boolean existsSimilarName = interestRepository.findAll().stream()
                .map(Interest::getName)
                .anyMatch(existingName -> calculateSimilarity(name, existingName) >= 0.8);

        if (existsSimilarName) {
            throw new SimilarInterestNameException();
        }
    }

    // 유사도 계산 로직
    private double calculateSimilarity(String source, String target) {
        int maxLength = Math.max(source.length(), target.length());

        if (maxLength == 0) {
            return 1.0;
        }

        int distance = calculateLevenshteinDistance(source, target);

        return 1.0 - ((double) distance / maxLength);
    }

    // Levenshtein Distance 계산 메서드
    private int calculateLevenshteinDistance(String source, String target) {
        int[][] dp = new int[source.length() + 1][target.length() + 1];

        for (int i = 0; i <= source.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= target.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= source.length(); i++) {
            for (int j = 1; j <= target.length(); j++) {
                int cost = source.charAt(i - 1) == target.charAt(j - 1)
                        ? 0
                        : 1;

                dp[i][j] = Math.min(
                        Math.min(
                                dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1
                        ),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[source.length()][target.length()];
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
