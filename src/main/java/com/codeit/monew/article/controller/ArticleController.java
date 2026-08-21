package com.codeit.monew.article.controller;

import com.codeit.monew.article.dto.command.ArticleSearchCommand;
import com.codeit.monew.article.dto.command.ArticleViewCreateCommand;
import com.codeit.monew.article.dto.request.ArticleSearchRequest;
import com.codeit.monew.article.dto.response.ArticleDto;
import com.codeit.monew.article.dto.response.ArticleViewDto;
import com.codeit.monew.article.dto.response.CursorPageResponseArticleDto;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.article.service.ArticleDeleteService;
import com.codeit.monew.article.service.ArticleQueryService;
import com.codeit.monew.article.service.ArticleViewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleQueryService articleService;
    private final ArticleViewService articleViewService;
    private final ArticleDeleteService articleDeleteService;

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

    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleDto> getArticle(
            @PathVariable UUID articleId,
            @RequestHeader("Monew-Request-User-ID")  UUID userId
    ) {
        return ResponseEntity.ok(articleService.getArticle(articleId, userId));
    }

    @GetMapping("/sources")
    public ResponseEntity<List<ArticleSource>> getSources() {
        return ResponseEntity.ok(articleService.getSources());
    }

    @PostMapping("/{articleId}/article-views")
    public ResponseEntity<ArticleViewDto> saveArticleView(
            @PathVariable UUID articleId,
            @RequestHeader("Monew-Request-User-ID") UUID userId
    ) {
        ArticleViewCreateCommand command =
                new ArticleViewCreateCommand(articleId, userId);

        return ResponseEntity.ok(articleViewService.save(command));
    }

    // 논리 삭제
    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> deleteArticle(
            @PathVariable UUID articleId
    ) {
        articleDeleteService.softDelete(articleId);
        return ResponseEntity.noContent().build();
    }

    // 물리 삭제
    @DeleteMapping("/{articleId}/hard")
    public ResponseEntity<Void> hardDeleteArticle(
            @PathVariable UUID articleId
    ) {
        articleDeleteService.hardDelete(articleId);
        return ResponseEntity.noContent().build();
    }

}
