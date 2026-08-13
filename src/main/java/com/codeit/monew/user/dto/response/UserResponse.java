package com.codeit.monew.user.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String nickname,
        Instant createdAt
) {
}