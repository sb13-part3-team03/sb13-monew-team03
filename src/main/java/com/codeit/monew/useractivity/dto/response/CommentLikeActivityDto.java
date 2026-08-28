package com.codeit.monew.useractivity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

public record CommentLikeActivityDto(
    @Schema(description = "좋아요 ID")
    UUID id,
    @Schema(description = "좋아요한 날짜")
    Instant createdAt,
    @Schema(description = "댓글 ID")
    UUID commentId,
    @Schema(description = "기사 ID")
    UUID articleId,
    @Schema(description = "기사 제목")
    String articleTitle,
    @Schema(description = "작성자 ID")
    UUID commentUserId,
    @Schema(description = "작성자 닉네임")
    String commentUserNickname,
    @Schema(description = "내용")
    String commentContent,
    @Schema(description = "좋아요 수")
    long commentLikeCount,
    @Schema(description = "작성된 날짜")
    Instant commentCreatedAt
) {
}
