package com.codeit.monew.interest.dto.request;

import com.codeit.monew.interest.service.condition.InterestSearchCondition;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

public record InterestSearchRequest(
        String keyword,

        @NotNull
        String orderBy,

        @NotNull
        String direction,

        String cursor,

        Instant after,

        @NotNull
        @Positive
        Integer limit

) {
    public InterestSearchCondition toCondition(UUID userId) {
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
