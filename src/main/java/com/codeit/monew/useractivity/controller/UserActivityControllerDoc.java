package com.codeit.monew.useractivity.controller;

import com.codeit.monew.useractivity.dto.response.UserActivityDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "사용자 활동 내역 관리", description = "사용자 활동 내역 관련 API")
public interface UserActivityControllerDoc {

  @Operation(summary = "사용자 활동 내역 조회", description = "사용자 ID로 활동 내역을 조회합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "사용자 활동 내역 조회 성공",
          content = @Content(schema = @Schema(implementation = UserActivityDto.class))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "사용자 정보 없음",
          content = @Content(schema = @Schema(implementation = UserActivityDto.class))
      ),
      @ApiResponse(
          responseCode = "500",
          description = "서버 내부 오류",
          content = @Content(schema = @Schema(implementation = UserActivityDto.class))
      )
  })
  ResponseEntity<UserActivityDto> find(
      @Parameter(description = "사용자 ID", required = true)
      @PathVariable UUID userId
  );
}
