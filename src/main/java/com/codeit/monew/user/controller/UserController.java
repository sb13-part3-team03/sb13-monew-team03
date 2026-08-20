package com.codeit.monew.user.controller;

import com.codeit.monew.auth.dto.request.LoginRequest;
import com.codeit.monew.auth.service.AuthService;
import com.codeit.monew.user.exception.UserForbiddenException;
import com.codeit.monew.user.dto.request.UserCreateRequest;
import com.codeit.monew.user.dto.request.UserUpdateRequest;
import com.codeit.monew.user.dto.response.UserResponse;
import com.codeit.monew.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final String REQUEST_USER_ID_HEADER = "Monew-Request-User-ID";

    private final UserService userService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody UserCreateRequest request
    ) {
        UserResponse response = userService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        UserResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID userId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requestUserId,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        validateUserAccess(userId, requestUserId);

        UserResponse response = userService.update(userId, request);

        return ResponseEntity.ok(response);
    }

    // 사용자 논리 삭제
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID userId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requestUserId
    ) {
        validateUserAccess(userId, requestUserId);

        userService.delete(userId);

        return ResponseEntity.noContent().build();
    }

    // 사용자 물리 삭제
    @DeleteMapping("/{userId}/hard")
    public ResponseEntity<Void> hardDelete(
            @PathVariable UUID userId,
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