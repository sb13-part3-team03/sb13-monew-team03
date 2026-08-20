package com.codeit.monew.interest.dto.request;

import com.codeit.monew.interest.service.condition.InterestSearchCondition;
import jakarta.validation.constraints.AssertTrue;
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

    @AssertTrue(message = "subscriberCount 정렬 시 cursor는 0 이상의 숫자여야 합니다.")
    public boolean isValidSubscriberCountCursor() {
        if (!"subscriberCount".equals(orderBy)
                || cursor == null
                || cursor.isBlank()) {
            return true;
        }

        try {
            return Long.parseLong(cursor) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

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
