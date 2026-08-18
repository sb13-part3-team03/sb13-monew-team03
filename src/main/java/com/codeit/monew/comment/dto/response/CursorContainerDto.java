package com.codeit.monew.comment.dto.response;

import java.util.List;

public record CursorContainerDto<T> (
        List<T> container,
        String nextCursor,
        String nextAfter,
        Long size,
        Long totalElement,
        Boolean hasNext
){
}
