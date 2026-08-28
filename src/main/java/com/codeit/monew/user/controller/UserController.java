package com.codeit.monew.user.controller;

import com.codeit.monew.auth.dto.request.LoginRequest;
import com.codeit.monew.auth.service.AuthService;
import com.codeit.monew.global.exception.ErrorResponse;
import com.codeit.monew.user.dto.request.UserCreateRequest;
import com.codeit.monew.user.dto.request.UserUpdateRequest;
import com.codeit.monew.user.dto.response.UserResponse;
import com.codeit.monew.user.exception.UserForbiddenException;
import com.codeit.monew.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(
        name = "사용자",
        description = "사용자 회원가입, 로그인, 정보 수정 및 삭제 API"
)
public class UserController {

    private static final String REQUEST_USER_ID_HEADER = "Monew-Request-User-ID";

    private final UserService userService;
    private final AuthService authService;

    @Operation(
            summary = "회원가입",
            description = "이메일, 닉네임, 비밀번호를 입력받아 새로운 사용자를 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UserResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이메일 형식, 닉네임, 비밀번호 검증 실패 등)",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 사용 중인 이메일",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody UserCreateRequest request
    ) {
        UserResponse response = userService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호를 검증하여 로그인합니다. 논리 삭제된 사용자는 로그인할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UserResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이메일 형식 등)",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "이메일 또는 비밀번호 불일치",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        UserResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "사용자 정보 수정",
            description = "사용자 ID를 기준으로 해당 사용자의 닉네임을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 수정 성공",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UserResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (닉네임 검증 실패, 잘못된 사용자 ID 형식 등)",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponse> update(
            @Parameter(
                    description = "수정할 사용자 ID",
                    required = true
            )
            @PathVariable UUID userId,

            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserResponse response = userService.update(userId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "사용자 논리 삭제",
            description = "사용자를 즉시 데이터베이스에서 제거하지 않고 삭제 시각을 기록하여 논리 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "사용자 논리 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (필수 헤더 누락, 잘못된 UUID 형식 등)",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "요청 사용자와 삭제 대상 사용자가 일치하지 않음",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(
            @Parameter(
                    description = "논리 삭제할 사용자 ID",
                    required = true
            )
            @PathVariable UUID userId,

            @Parameter(
                    description = "요청을 수행한 사용자 ID",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requestUserId
    ) {
        validateUserAccess(userId, requestUserId);

        userService.delete(userId);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "사용자 물리 삭제",
            description = "사용자 데이터를 데이터베이스에서 완전히 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "사용자 물리 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (필수 헤더 누락, 잘못된 UUID 형식 등)",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "요청 사용자와 삭제 대상 사용자가 일치하지 않음",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @DeleteMapping("/{userId}/hard")
    public ResponseEntity<Void> hardDelete(
            @Parameter(
                    description = "물리 삭제할 사용자 ID",
                    required = true
            )
            @PathVariable UUID userId,

            @Parameter(
                    description = "요청을 수행한 사용자 ID",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requestUserId
    ) {
        validateUserAccess(userId, requestUserId);

        userService.hardDelete(userId);

        return ResponseEntity.noContent().build();
    }

    private void validateUserAccess(
            UUID userId,
            UUID requestUserId
    ) {
        if (!userId.equals(requestUserId)) {
            throw new UserForbiddenException();
        }
    }
}