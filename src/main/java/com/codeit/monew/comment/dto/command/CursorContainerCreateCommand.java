package com.codeit.monew.comment.dto.command;

import java.util.List;

public record CursorContainerCreateCommand<T>(
        List<T> contents,
        String nextCursor,
        String nextAfter,
        Long size,
        Long totalElement,
        Boolean hasNext
) {
}
