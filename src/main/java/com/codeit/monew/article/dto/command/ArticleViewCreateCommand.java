package com.codeit.monew.article.dto.command;

import java.util.UUID;

public record ArticleViewCreateCommand(
        UUID articleId,
        UUID userId
) {
}
