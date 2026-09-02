package com.codeit.monew.article.controller;

import com.codeit.monew.article.dto.command.ArticleSearchCommand;
import com.codeit.monew.article.dto.response.ArticleDto;
import com.codeit.monew.article.dto.response.ArticleViewDto;
import com.codeit.monew.article.dto.response.CursorPageResponseArticleDto;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.article.service.ArticleDeleteService;
import com.codeit.monew.article.service.ArticleQueryService;
import com.codeit.monew.article.service.ArticleRestoreService;
import com.codeit.monew.article.service.ArticleViewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
public class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleQueryService articleQueryService;

    @MockitoBean
    private ArticleViewService articleViewService;

    @MockitoBean
    private ArticleDeleteService articleDeleteService;

    @MockitoBean
    private ArticleRestoreService articleRestoreService;

    @Test
    @DisplayName("뉴스 기사 목록을 조회한다")
    void searchArticles() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        CursorPageResponseArticleDto response =
                new CursorPageResponseArticleDto(
                        List.of(), // content
                        null,      // nextCursor
                        null,      // nextAfter
                        null,      // nextAfterId
                        0,         // size
                        0L,        // totalElements
                        false      // hasNext
                );

        when(articleQueryService.searchArticles(any(ArticleSearchCommand.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/articles")
                        .header("Monew-Request-User-ID", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.hasNext").value(false));

        verify(articleQueryService)
                .searchArticles(any(ArticleSearchCommand.class));
    }

    @Test
    @DisplayName("뉴스 기사 단건을 조회한다")
    void getArticle() throws Exception {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ArticleDto response = /* 실제 ArticleDto 생성 */ null;

        when(articleQueryService.getArticle(articleId, userId))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/articles/{articleId}", articleId)
                        .header("Monew-Request-User-ID", userId))
                .andExpect(status().isOk());

        verify(articleQueryService)
                .getArticle(articleId, userId);
    }

    @Test
    @DisplayName("기사 출처 목록을 조회한다")
    void getSources() throws Exception {
        // given
        when(articleQueryService.getSources())
                .thenReturn(List.of(
                        ArticleSource.NAVER,
                        ArticleSource.HANKYUNG,
                        ArticleSource.CHOSUN,
                        ArticleSource.YEONHAP
                ));

        // when & then
        mockMvc.perform(get("/api/articles/sources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("NAVER"))
                .andExpect(jsonPath("$[1]").value("HANKYUNG"))
                .andExpect(jsonPath("$[2]").value("CHOSUN"))
                .andExpect(jsonPath("$[3]").value("YEONHAP"));
    }

    @Test
    @DisplayName("기사 조회 기록을 저장한다")
    void saveArticleView() throws Exception {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ArticleViewDto response = /* 실제 ArticleViewDto 생성 */ null;

        when(articleViewService.save(any()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/articles/{articleId}/article-views", articleId)
                        .header("Monew-Request-User-ID", userId))
                .andExpect(status().isOk());

        verify(articleViewService)
                .save(any());
    }

    @Test
    @DisplayName("기사를 논리 삭제한다")
    void deleteArticle() throws Exception {
        // given
        UUID articleId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/articles/{articleId}", articleId))
                .andExpect(status().isNoContent());

        verify(articleDeleteService)
                .softDelete(articleId);
    }

    @Test
    @DisplayName("기사를 물리 삭제한다")
    void hardDeleteArticle() throws Exception {
        // given
        UUID articleId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/articles/{articleId}/hard", articleId))
                .andExpect(status().isNoContent());

        verify(articleDeleteService)
                .hardDelete(articleId);
    }

    @Test
    @DisplayName("기간에 해당하는 기사를 복구한다")
    void restore() throws Exception {
        // given
        LocalDate from = LocalDate.of(2026, 8, 24);
        LocalDate to = LocalDate.of(2026, 8, 25);

        when(articleRestoreService.restore(from, to))
                .thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/articles/restore")
                        .param("from", "2026-08-24T00:00:00")
                        .param("to", "2026-08-25T23:59:59"))
                .andExpect(status().isOk());

        verify(articleRestoreService)
                .restore(from, to);
    }

    @Test
    @DisplayName("복구 시작일이 종료일보다 늦으면 예외가 발생한다")
    void restore_invalidDate() throws Exception {
        // when & then
        mockMvc.perform(get("/api/articles/restore")
                        .param("from", "2026-08-26T00:00:00")
                        .param("to", "2026-08-24T00:00:00"))
                .andExpect(status().is4xxClientError());
    }

}
