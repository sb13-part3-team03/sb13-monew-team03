package com.codeit.monew.notification.dto.request;

import com.codeit.monew.notification.condition.NotificationSearchCondition;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record NotificationSearchRequest(
    @Parameter(description = "커서 값")
    @Pattern(
        regexp = "(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
        message = "cursor는 UUID 형식이어야 합니다."
    )
    String cursor,
    @Parameter(description = "보조 커서(createdAt) 값")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
    @Parameter(description = "커서 페이지 크기", required = true, example = "50")
    @Positive int limit
) {

  public NotificationSearchCondition toCondition(UUID userId) {
    return new NotificationSearchCondition(userId, cursor, after, limit);
  }
}
