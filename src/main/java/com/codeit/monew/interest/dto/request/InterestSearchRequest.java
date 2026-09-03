package com.codeit.monew.interest.dto.request;

import com.codeit.monew.interest.service.condition.InterestSearchCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

public record InterestSearchRequest(
        @Schema(
                description = "검색어(관심사 이름, 키워드)",
                example = "스포츠"
        )
        String keyword,

        @Schema(
                description = "정렬 속성 이름",
                allowableValues = {"name", "subscriberCount"}
        )
        @NotNull
        @Pattern(regexp = "name|subscriberCount")
        String orderBy,

        @Schema(
                description = "정렬 방향 (ASC, DESC)",
                allowableValues = {"ASC", "DESC"}
        )
        @NotNull
        @Pattern(
                regexp = "ASC|DESC",
                flags = Pattern.Flag.CASE_INSENSITIVE
        )
        String direction,

        @Schema(
                description = "커서 값"
        )
        String cursor,

        @Schema(
                description = "보조 커서(createdAt) 값"
        )
        Instant after,

        @Schema(
                description = "커서 페이지 크기",
                example = "50"
        )
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
