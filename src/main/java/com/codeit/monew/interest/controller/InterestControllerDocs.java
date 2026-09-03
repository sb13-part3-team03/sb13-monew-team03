package com.codeit.monew.interest.controller;

import com.codeit.monew.interest.dto.request.InterestRegisterRequest;
import com.codeit.monew.interest.dto.request.InterestSearchRequest;
import com.codeit.monew.interest.dto.request.InterestUpdateRequest;
import com.codeit.monew.interest.dto.response.CursorPageResponseInterestDto;
import com.codeit.monew.interest.dto.response.InterestDto;
import com.codeit.monew.interest.dto.response.SubscriptionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(
        name = "관심사 관리",
        description = "관심사 관련 API"
)
public interface InterestControllerDocs {
    @Operation(
            summary = "관심사 목록 조회",
            description = "조건에 맞는 관심사 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(
                                    implementation = CursorPageResponseInterestDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류 등)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content
            )
    })
    ResponseEntity<CursorPageResponseInterestDto> findInterests(
            @ParameterObject
            InterestSearchRequest request,

            @Parameter(
                    description = "요청자 ID",
                    required = true
            )
            UUID userId
    );

    @Operation(
            summary = "관심사 등록",
            description = "새로운 관심사를 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "등록 성공",
                    content = @Content(
                            schema = @Schema(implementation = InterestDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력값 검증 실패)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "유사 관심사 중복",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content
            )
    })
    ResponseEntity<InterestDto> createInterest(
            InterestRegisterRequest request
    );

    @Operation(
            summary = "관심사 구독",
            description = "관심사를 구독합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "구독 성공",
                    content = @Content(
                            schema = @Schema(implementation = SubscriptionDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "관심사 또는 사용자 정보 없음",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 구독 중인 관심사",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content
            )
    })
    ResponseEntity<SubscriptionDto> subscribe(
            @Parameter(
                    description = "관심사 ID",
                    required = true
            )
            UUID interestId,

            @Parameter(
                    description = "요청자 ID",
                    required = true
            )
            UUID userId
    );

    @Operation(
            summary = "관심사 구독 취소",
            description = "관심사 구독을 취소합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "구독 취소 성공",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "구독 정보 없음",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content
            )
    })
    ResponseEntity<Void> unsubscribe(
            @Parameter(
                    description = "관심사 ID",
                    required = true
            )
            UUID interestId,

            @Parameter(
                    description = "요청자 ID",
                    required = true
            )
            UUID userId
    );

    @Operation(
            summary = "관심사 물리 삭제",
            description = "관심사를 물리적으로 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "삭제 성공",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "관심사 정보 없음",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content
            )
    })
    ResponseEntity<Void> deleteInterest(
            @Parameter(
                    description = "관심사 ID",
                    required = true
            )
            UUID interestId
    );

    @Operation(
            summary = "관심사 정보 수정",
            description = "관심사의 키워드를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            schema = @Schema(implementation = InterestDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력값 검증 실패)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "관심사 정보 없음",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content
            )
    })
    ResponseEntity<InterestDto> updateInterest(
            @Parameter(
                    description = "관심사 ID",
                    required = true
            )
            UUID interestId,
            InterestUpdateRequest request
    );
}
