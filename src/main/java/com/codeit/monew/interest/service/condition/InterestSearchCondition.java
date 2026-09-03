package com.codeit.monew.interest.service.condition;

import org.hibernate.query.SortDirection;

import java.time.Instant;
import java.util.UUID;

public record InterestSearchCondition(
        String keyword,
        String orderBy,
        String direction,
        String cursor,
        Instant after,
        int limit,
        UUID userId

) {
}
