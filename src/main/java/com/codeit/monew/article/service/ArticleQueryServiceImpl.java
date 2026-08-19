package com.codeit.monew.article.service;

import com.codeit.monew.article.dto.command.ArticleSearchCommand;
import com.codeit.monew.article.dto.response.ArticleDto;
import com.codeit.monew.article.dto.response.ArticleSearchResult;
import com.codeit.monew.article.dto.response.CursorPageResponseArticleDto;
import com.codeit.monew.article.mapper.ArticleMapper;
import com.codeit.monew.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
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

        // limit + 1개 조회
        List<ArticleSearchResult> results = articleRepository.searchArticles(command, orderBy);

        // 다음 페이지 존재 여부
        boolean hasNext = results.size() > command.limit();

        // 실제 응답 데이터
        List<ArticleSearchResult> content = hasNext
                ? results.subList(0, command.limit())
                : results;

        // 마지막 데이터
        ArticleSearchResult last = content.isEmpty() ? null : content.get(content.size() - 1);

        String nextCursor = null;
        UUID nextAfter = null;

        if (hasNext && last != null) {
            nextCursor = createNextCursor(last, orderBy);
            nextAfter = last.article().getId();
        }

        // 전체 검색 결과 개수
        long totalElements = articleRepository.countTotalElements(command);

        List<ArticleDto> articleDtos = articleMapper.toDtoList(content);

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

    private String normalizeOrderBy(String orderBy) {
        return StringUtils.hasText(orderBy)
                ? orderBy
                : "publishDate";
    }

}