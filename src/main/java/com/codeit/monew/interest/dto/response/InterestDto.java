package com.codeit.monew.interest.dto.response;

import com.codeit.monew.interest.entity.Interest;

import java.util.List;
import java.util.UUID;

public record InterestDto(
        UUID id,
        String name,
        List<String> keywords,
        long subscriberCount,
        boolean subscribedByMe

) {
    public static InterestDto from(Interest interest, long subscriberCount, boolean subscribedByMe) {
        return new InterestDto(
                interest.getId(),
                interest.getName(),
                List.copyOf(interest.getKeywords()),
                subscriberCount,
                subscribedByMe
        );
    }
}
