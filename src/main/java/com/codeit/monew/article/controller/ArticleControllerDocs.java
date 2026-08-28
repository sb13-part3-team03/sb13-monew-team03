package com.codeit.monew.article.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface ArticleControllerDocs {

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
}