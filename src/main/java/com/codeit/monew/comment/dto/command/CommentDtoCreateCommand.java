package com.codeit.monew.comment.dto.command;

import java.time.Instant;
import java.util.UUID;

public record CommentDtoCreateCommand(
        UUID id,
        UUID articleId,
        UUID userId,
        String userNickName,
        String content,
        Long likeCount,
        Boolean likeByMe,
        Instant createdAt
) {
    @Override
    public String toString() {
        return "CommentDtoCreateCommand{" +
                "id=" + id +
                ", articleId=" + articleId +
                ", userId=" + userId +
                ", userNickName='" + userNickName + '\'' +
                ", content='" + content + '\'' +
                ", likeCount=" + likeCount +
                ", likeByMe=" + likeByMe +
                ", createdAt=" + createdAt +
                '}';
    }
}
