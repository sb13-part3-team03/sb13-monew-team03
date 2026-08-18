package com.codeit.monew.article.service;

import com.codeit.monew.article.dto.request.ArticleSearchRequest;
import com.codeit.monew.article.dto.response.ArticleDto;
import com.codeit.monew.article.dto.response.ArticleSearchResult;
import com.codeit.monew.article.dto.response.CursorPageResponseArticleDto;
import com.codeit.monew.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleQueryServiceImpl implements ArticleQueryService {

    private final ArticleRepository articleRepository;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseArticleDto searchArticles(
            ArticleSearchRequest request,
            UUID userId
    ) {
        // limit + 1개 조회
        List<ArticleSearchResult> results = articleRepository.searchArticles(request);

        // 다음 페이지 존재 여부
        boolean hasNext = results.size() > request.limit();

        // 실제 응답 데이터
        List<ArticleSearchResult> content = hasNext
                ? results.subList(0, request.limit())
                : results;

        // 마지막 데이터
        ArticleSearchResult last = content.isEmpty() ? null : content.get(content.size() - 1);

        String nextCursor = null;
        UUID nextAfter = null;

        if (hasNext && last != null) {
            nextCursor = createNextCursor(last, request.orderBy());

            nextAfter = last.article().getId();
        }

        // 전체 검색 결과 개수
        long totalElements = articleRepository.countTotalElements(request);

        List<ArticleDto> articleDtos = content.stream()
                .map(ArticleDto::from)
                .toList();

        return new CursorPageResponseArticleDto(
                articleDtos,
                nextCursor,
                nextAfter,
                request.limit(),
                totalElements,
                hasNext
        );
    }

    private String createNextCursor(
            ArticleSearchResult result,
            String orderBy
    ) {
        return switch (orderBy) {
            case "commentCount" ->
                    String.valueOf(result.commentCount());

            case "viewCount" ->
                    String.valueOf(result.viewCount());

            default ->
                    result.article().getPublishDate().toString();
        };
    }


}