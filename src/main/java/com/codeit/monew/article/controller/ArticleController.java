package com.codeit.monew.article.controller;

import com.codeit.monew.article.dto.command.ArticleSearchCommand;
import com.codeit.monew.article.dto.request.ArticleSearchRequest;
import com.codeit.monew.article.dto.response.CursorPageResponseArticleDto;
import com.codeit.monew.article.service.ArticleQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleQueryService articleService;

    // 뉴스 기사 검색 목록 커서페이지네이션 조회
    @GetMapping("")
    public ResponseEntity<CursorPageResponseArticleDto> searchArticles(
            @Valid @ModelAttribute ArticleSearchRequest request,
            @RequestHeader("Monew-Request-User-ID") UUID userId
    ) {
        ArticleSearchCommand command = ArticleSearchCommand.from(request, userId);

        CursorPageResponseArticleDto response = articleService.searchArticles(command);

        return ResponseEntity.ok(response);
    }

}
