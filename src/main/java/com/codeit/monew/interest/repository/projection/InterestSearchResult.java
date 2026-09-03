package com.codeit.monew.interest.repository.projection;

import com.codeit.monew.interest.entity.Interest;

public record InterestSearchResult(
        Interest interest,
        long subscriberCount,
        boolean subscribedByMe
) {
}
