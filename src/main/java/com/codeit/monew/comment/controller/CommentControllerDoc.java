package com.codeit.monew.comment.controller;

import com.codeit.monew.comment.dto.request.CommentRegisterRequest;
import com.codeit.monew.comment.dto.request.CommentUpdateRequest;
import com.codeit.monew.comment.dto.response.CommentDto;
import com.codeit.monew.comment.dto.response.CommentLikeDto;
import com.codeit.monew.comment.dto.response.CursorContainerDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "댓글 관리",description = "댓글 관련 API")
public interface CommentControllerDoc {

    @GetMapping
    @Operation(
            summary = "댓글 목록 조회",
            description = "조건에 따라 댓글 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 정보 오류"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    public ResponseEntity<CursorContainerDto<CommentDto>> getComments(
            @Parameter(description = "기사 ID", example = "articleId")
            @RequestParam(required = false) UUID articleId,

            @Parameter(
                    description = "정렬 속성 이름", required = true,
                    schema = @Schema(allowableValues = {"createdAt", "likeCount"})
            )
            @RequestParam String orderBy,

            @Parameter(
                    description = "정렬 방향", required = true,
                    schema = @Schema(allowableValues = {"ASC", "DESC"})
            )
            @RequestParam String direction,

            @Parameter(description = "커서 값", example = "cursor")
            @RequestParam(required = false) String cursor,

            @Parameter(description = "보조 커서 값", example = "after")
            @RequestParam(required = false) String after,

            @Parameter(description = "커서 페이지 크기", example = "50", required = true)
            @RequestParam Integer limit,

            @Parameter(description = "요청자 ID", required = true)
            @RequestHeader(value = "Monew-Request-User-Id") UUID userId
    );

    @PostMapping
    @Operation(
            summary = "댓글 등록",
            description = "새 댓글을 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "등록 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "입력 정보 오류"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    public ResponseEntity<CommentDto> createComment(
            @Validated @RequestBody CommentRegisterRequest request
    );


    @PatchMapping("/{commentId}")
    @Operation(
            summary = "댓글 수정",
            description = "기존 댓글을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "수정 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "입력 정보 오류"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "댓글 정보 없음"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    public ResponseEntity<CommentDto> modifiedComment(
            @Parameter(description = "댓글ID", required = true)
            @PathVariable UUID commentId,
            @Validated @RequestBody CommentUpdateRequest request,
            @Parameter(description = "요청자 ID", required = true)
            @RequestHeader(value = "Monew-Request-User-Id") UUID userId
    );

    // getting user id from header?
    @DeleteMapping("/{commentId}")
    @Operation(
            summary = "댓글 논리 삭제",
            description = "댓글을 논리적으로 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "삭제 성공",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "댓글 정보 없음",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content
            )
    })
    public ResponseEntity<?> logicalDeleteComment(
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable UUID commentId
    );

    @DeleteMapping("/{commentId}/hard")
    @Operation(
            summary = "댓글 물리 삭제",
            description = "댓글과 연관된 정보를 물리적으로 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "삭제 성공",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "댓글 정보 없음",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content
            )
    })
    public ResponseEntity<?> deleteComment(
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable UUID commentId
    );


    /*
    CommentLike API
     */


    @PostMapping("/{commentId}/comment-likes")
    @Operation(
            summary = "좋아요 등록",
            description = "댓글에 좋아요를 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "좋아요 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CommentLikeDto.class
                            )
                    )

            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "댓글 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CommentLikeDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CommentLikeDto.class
                            )
                    )
            )
    })
    public ResponseEntity<?> registryCommentLike(
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable UUID commentId,
            @Parameter(description = "요청자 ID", required = true)
            @RequestHeader(value = "Monew-Request-User-Id") UUID userId
    );


    @DeleteMapping("/{commentId}/comment-likes")
    @Operation(
            summary = "좋아요 취소",
            description = "댓글의 좋아요를 취소합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "좋아요 취소 성공",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "댓글 정보 없음",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content
            )
    })
    public ResponseEntity<?> cancelCommentLike(
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable UUID commentId,
            @Parameter(description = "요청자 ID", required = true)
            @RequestHeader(value = "Monew-Request-User-Id") UUID userId
    );

}
