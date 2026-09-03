package com.codeit.monew.interest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;


@Schema(description = "커서 기반 페이지 응답")
public record CursorPageResponseInterestDto(
        List<InterestDto> content,
        String nextCursor,
        Instant nextAfter,
        int size,
        long totalElements,
        boolean hasNext

) {
}
