package com.codeit.monew.comment.dto.response;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CursorContainerDto<T>(

        @JsonProperty("content")
        List<T> content,

        String nextCursor,

        String nextAfter,

        Long size,

        @JsonProperty("totalElements")
        Long totalElements,

        Boolean hasNext

) {
}