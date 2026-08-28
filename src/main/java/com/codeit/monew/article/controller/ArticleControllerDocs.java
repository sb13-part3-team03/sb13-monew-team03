package com.codeit.monew.article.controller;

import com.codeit.monew.article.dto.request.ArticleSearchRequest;
import com.codeit.monew.article.dto.response.ArticleDto;
import com.codeit.monew.article.dto.response.ArticleRestoreResultDto;
import com.codeit.monew.article.dto.response.ArticleViewDto;
import com.codeit.monew.article.dto.response.CursorPageResponseArticleDto;
import com.codeit.monew.article.entity.ArticleSource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ArticleControllerDocs {

    @Operation(
            summary = "뉴스 기사 목록 조회",
            description = "조건에 맞는 뉴스 기사 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = CursorPageResponseArticleDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청(정렬 기준 오류, 페이지네이션 파라미터 오류 등)",
                    content = @Content(
                            schema = @Schema(implementation = CursorPageResponseArticleDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            schema = @Schema(implementation = CursorPageResponseArticleDto.class)
                    )
            )
    })
    ResponseEntity<CursorPageResponseArticleDto> searchArticles(
            @ParameterObject
            ArticleSearchRequest request,
            @Parameter(description = "요청자 ID", required = true)
            @RequestHeader("Monew-Request-User-ID")
            UUID userId
    );

    @Operation(
            summary = "뉴스 기사 단건 조회",
            description = "뉴스 기사 ID로 뉴스 기사 단건을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = ArticleDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "뉴스 기사 정보 없음",
                    content = @Content(
                            schema = @Schema(implementation = ArticleDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            schema = @Schema(implementation = ArticleDto.class)
                    )
            )
    })
    ResponseEntity<ArticleDto> getArticle(
            @Parameter(description = "뉴스 기사 ID", required = true)
            UUID articleId,
            @Parameter(description = "요청자 ID", required = true)
            @RequestHeader("Monew-Request-User-ID")
            UUID userId
    );

    @Operation(
            summary = "출처 목록 조회",
            description = "출처 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ArticleSource.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "내부 서버 오류",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ArticleSource.class)
                            )
                    )
            )
    })
    ResponseEntity<List<ArticleSource>> getSources();

    @Operation(
            summary = "기사 뷰 등록",
            description = "기사 뷰를 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "기사 뷰 등록 성공",
                    content = @Content(
                            schema = @Schema(implementation = ArticleViewDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "댓글 정보 없음",
                    content = @Content(
                            schema = @Schema(implementation = ArticleViewDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            schema = @Schema(implementation = ArticleViewDto.class)
                    )
            )
    })
    ResponseEntity<ArticleViewDto> saveArticleView(
            @Parameter(description = "기사 ID", required = true)
            UUID articleId,
            @Parameter(description = "요청자 ID", required = true)
            @RequestHeader("Monew-Request-User-ID")
            UUID userId
    );

    @Operation(
            summary = "뉴스 기사 논리 삭제",
            description = "뉴스 기사를 논리적으로 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "논리 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "뉴스 기사 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<Void> deleteArticle(
            @Parameter(description = "뉴스 기사 ID", required = true)
            UUID articleId
    );

    @Operation(
            summary = "뉴스 기사 물리 삭제",
            description = "뉴스 기사를 물리적으로 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "물리 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "뉴스 기사 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<Void> hardDeleteArticle(
            @Parameter(description = "뉴스 기사 ID", required = true)
            UUID articleId
    );

    @Operation(
            summary = "뉴스 복구",
            description = "유실된 뉴스 기사를 복구."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "복구 성공",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = ArticleRestoreResultDto.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = ArticleRestoreResultDto.class
                                    )
                            )
                    )
            )
    })
    ResponseEntity<List<ArticleRestoreResultDto>> restore(
            @Parameter(description = "날짜 시작(범위)")
            @RequestParam
            LocalDateTime from,
            @Parameter(description = "날짜 끝(범위)")
            @RequestParam
            LocalDateTime to
    );

}