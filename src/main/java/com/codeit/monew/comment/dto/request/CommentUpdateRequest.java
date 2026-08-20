package com.codeit.monew.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CommentUpdateRequest(
        @NotBlank @Length(max = 500) String content
) {
}
