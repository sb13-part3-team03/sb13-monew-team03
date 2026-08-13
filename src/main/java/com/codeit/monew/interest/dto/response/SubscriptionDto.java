package com.codeit.monew.interest.dto.response;

import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Subscription;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SubscriptionDto(
        UUID id,
        UUID interestId,
        String interestName,
        List<String> interestKeywords,
        long interestSubscriberCount,
        Instant createdAt

) {
    public static SubscriptionDto from(Subscription subscription, long interestSubscriberCount) {
        Interest interest = subscription.getInterest();

        return new SubscriptionDto(
                subscription.getId(),
                interest.getId(),
                interest.getName(),
                List.copyOf(interest.getKeywords()),
                interestSubscriberCount,
                subscription.getCreatedAt()
        );
    }
}
