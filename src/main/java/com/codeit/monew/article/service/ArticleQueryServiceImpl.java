package com.codeit.monew.article.service;

import com.codeit.monew.article.dto.command.ArticleSearchCommand;
import com.codeit.monew.article.dto.response.ArticleDto;
import com.codeit.monew.article.dto.response.ArticleSearchResult;
import com.codeit.monew.article.dto.response.CursorPageResponseArticleDto;
import com.codeit.monew.article.mapper.ArticleMapper;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.global.exception.MonewException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleQueryServiceImpl implements ArticleQueryService {

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseArticleDto searchArticles(ArticleSearchCommand command) {
        String orderBy = normalizeOrderBy(command.orderBy());

        validateDirection(command.direction());
        validatePagination(command, orderBy);

        List<ArticleSearchResult> results =
                articleRepository.searchArticles(command, orderBy);

        boolean hasNext = results.size() > command.limit();

        List<ArticleSearchResult> content = hasNext
                ? results.subList(0, command.limit())
                : results;

        ArticleSearchResult last =
                content.isEmpty()
                        ? null
                        : content.get(content.size() - 1);

        String nextCursor = null;
        UUID nextAfter = null;

        if (hasNext && last != null) {
            nextCursor = createNextCursor(last, orderBy);
            nextAfter = last.article().getId();
        }

        long totalElements =
                articleRepository.countTotalElements(command);

        List<ArticleDto> articleDtos =
                articleMapper.toDtoList(content);

        return new CursorPageResponseArticleDto(
                articleDtos,
                nextCursor,
                nextAfter,
                command.limit(),
                totalElements,
                hasNext
        );
    }

    private String createNextCursor(
            ArticleSearchResult result,
            String orderBy
    ) {
        if ("commentCount".equals(orderBy)) {
            return String.valueOf(result.commentCount());
        }

        if ("viewCount".equals(orderBy)) {
            return String.valueOf(result.viewCount());
        }

        return result.article().getPublishDate().toString();
    }

    // 정렬 기준 유효성 검증
    private String normalizeOrderBy(String orderBy) {
        if (!StringUtils.hasText(orderBy)) {
            return "publishDate";
        }

        if (!Set.of("publishDate", "commentCount", "viewCount").contains(orderBy)) {
            throw new MonewException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return orderBy;
    }

    // 정렬 유효성 검증
    private void validateDirection(String direction) {
        if (!Set.of("asc", "desc").contains(direction.toLowerCase())) {
            throw new MonewException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // 페이지네이션 유효성 검증
    private void validatePagination(
            ArticleSearchCommand command,
            String orderBy
    ) {
        boolean hasCursor = StringUtils.hasText(command.cursor());
        boolean hasAfter = command.after() != null;

        if (hasCursor != hasAfter) {
            throw new MonewException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (!hasCursor) {
            return;
        }

        try {
            if ("publishDate".equals(orderBy)) {
                Instant.parse(command.cursor());
            } else {
                Long.parseLong(command.cursor());
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new MonewException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

}