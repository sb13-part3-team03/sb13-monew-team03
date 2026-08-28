package com.codeit.monew.article.controller;

import com.codeit.monew.article.dto.command.ArticleSearchCommand;
import com.codeit.monew.article.dto.command.ArticleViewCreateCommand;
import com.codeit.monew.article.dto.request.ArticleSearchRequest;
import com.codeit.monew.article.dto.response.ArticleDto;
import com.codeit.monew.article.dto.response.ArticleRestoreResultDto;
import com.codeit.monew.article.dto.response.ArticleViewDto;
import com.codeit.monew.article.dto.response.CursorPageResponseArticleDto;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.article.exception.ArticleRestoreException;
import com.codeit.monew.article.service.ArticleDeleteService;
import com.codeit.monew.article.service.ArticleQueryService;
import com.codeit.monew.article.service.ArticleRestoreService;
import com.codeit.monew.article.service.ArticleViewService;
import com.codeit.monew.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
@Slf4j
public class ArticleController implements ArticleControllerDocs {

    private final ArticleQueryService articleService;
    private final ArticleViewService articleViewService;
    private final ArticleDeleteService articleDeleteService;
    private final ArticleRestoreService articleRestoreService;

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

    // 기사 단건 조회
    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleDto> getArticle(
            @PathVariable UUID articleId,
            @RequestHeader("Monew-Request-User-ID")  UUID userId
    ) {
        return ResponseEntity.ok(articleService.getArticle(articleId, userId));
    }

    // 기사 출처 목록 조회
    @GetMapping("/sources")
    public ResponseEntity<List<ArticleSource>> getSources() {
        return ResponseEntity.ok(articleService.getSources());
    }

    // 기사 조회 기록 저장
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

    // 기사 복구
    @GetMapping("/restore")
    public ResponseEntity<List<ArticleRestoreResultDto>> restore(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        log.info("기사 복구 요청. from={}, to={}", from, to);

        if (from.isAfter(to)) {
            throw new ArticleRestoreException(
                    ErrorCode.INVALID_RESTORE_DATE
            );
        }

        List<ArticleRestoreResultDto> result =
                articleRestoreService.restore(
                    from.toLocalDate(),
                    to.toLocalDate()
                );

        log.info("기사 복구 요청 완료. from={}, to={}", from, to);

        return ResponseEntity.ok(result);
    }

}
