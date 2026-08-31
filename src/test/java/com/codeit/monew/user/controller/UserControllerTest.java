package com.codeit.monew.user.controller;

import com.codeit.monew.auth.dto.request.LoginRequest;
import com.codeit.monew.auth.service.AuthService;
import com.codeit.monew.global.exception.GlobalExceptionHandler;
import com.codeit.monew.user.dto.request.UserCreateRequest;
import com.codeit.monew.user.dto.request.UserUpdateRequest;
import com.codeit.monew.user.dto.response.UserResponse;
import com.codeit.monew.user.exception.DuplicateEmailException;
import com.codeit.monew.user.exception.LoginFailedException;
import com.codeit.monew.user.exception.UserNotFoundException;
import com.codeit.monew.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController 단위 테스트")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("유효한 회원가입 요청이면 사용자를 생성한다.")
    void createUser_whenValidRequest_returnsCreated() throws Exception {

        // given
        UserCreateRequest request = new UserCreateRequest(
                "test@example.com",
                "테스트",
                "password1234"
        );

        UserResponse response = new UserResponse(
                UUID.randomUUID(),
                "test@example.com",
                "테스트",
                Instant.now()
        );

        given(userService.create(any(UserCreateRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("테스트"));
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 400 응답을 반환한다.")
    void createUser_whenInvalidEmail_returnsBadRequest() throws Exception {

        // given
        UserCreateRequest request = new UserCreateRequest(
                "invalid-email",
                "테스트",
                "password1234"
        );

        // when & then
        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입하면 409 응답을 반환한다.")
    void createUser_whenEmailAlreadyExists_returnsConflict() throws Exception {

        // given
        UserCreateRequest request = new UserCreateRequest(
                "test@example.com",
                "테스트",
                "password1234"
        );

        given(userService.create(any(UserCreateRequest.class)))
                .willThrow(new DuplicateEmailException());

        // when & then
        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("이메일과 비밀번호가 일치하면 로그인에 성공한다.")
    void login_whenCredentialsAreValid_returnsOk() throws Exception {

        // given
        LoginRequest request = new LoginRequest(
                "test@example.com",
                "password1234"
        );

        UserResponse response = new UserResponse(
                UUID.randomUUID(),
                "test@example.com",
                "테스트",
                Instant.now()
        );

        given(authService.login(any(LoginRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("테스트"));
    }

    @Test
    @DisplayName("이메일 또는 비밀번호가 일치하지 않으면 401 응답을 반환한다.")
    void login_whenCredentialsAreInvalid_returnsUnauthorized() throws Exception {

        // given
        LoginRequest request = new LoginRequest(
                "test@example.com",
                "wrong-password"
        );

        given(authService.login(any(LoginRequest.class)))
                .willThrow(new LoginFailedException());

        // when & then
        mockMvc.perform(
                        post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인 요청 이메일 형식이 올바르지 않으면 400 응답을 반환한다.")
    void login_whenEmailIsInvalid_returnsBadRequest() throws Exception {

        // given
        LoginRequest request = new LoginRequest(
                "invalid-email",
                "password1234"
        );

        // when & then
        mockMvc.perform(
                        post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("사용자 닉네임 수정에 성공하면 200 응답을 반환한다.")
    void updateUser_whenValidRequest_returnsOk() throws Exception {

        // given
        UUID userId = UUID.randomUUID();

        UserUpdateRequest request =
                new UserUpdateRequest("새닉네임");

        UserResponse response = new UserResponse(
                userId,
                "test@example.com",
                "새닉네임",
                Instant.now()
        );

        given(userService.update(eq(userId), any(UserUpdateRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        patch("/api/users/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("새닉네임"));

        verify(userService)
                .update(eq(userId), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("닉네임이 비어있으면 400 응답을 반환한다.")
    void updateUser_whenNicknameIsBlank_returnsBadRequest() throws Exception {

        // given
        UUID userId = UUID.randomUUID();

        UserUpdateRequest request =
                new UserUpdateRequest("");

        // when & then
        mockMvc.perform(
                        patch("/api/users/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .update(eq(userId), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 수정하면 404 응답을 반환한다.")
    void updateUser_whenUserDoesNotExist_returnsNotFound() throws Exception {

        // given
        UUID userId = UUID.randomUUID();

        UserUpdateRequest request =
                new UserUpdateRequest("새닉네임");

        given(userService.update(eq(userId), any(UserUpdateRequest.class)))
                .willThrow(new UserNotFoundException());

        // when & then
        mockMvc.perform(
                        patch("/api/users/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        verify(userService)
                .update(eq(userId), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("사용자 논리 삭제에 성공하면 204 응답을 반환한다.")
    void deleteUser_whenValidRequest_returnsNoContent() throws Exception {

        // given
        UUID userId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        delete("/api/users/{userId}", userId)
                )
                .andExpect(status().isNoContent());

        verify(userService).delete(userId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 삭제하면 404 응답을 반환한다.")
    void deleteUser_whenUserDoesNotExist_returnsNotFound() throws Exception {

        // given
        UUID userId = UUID.randomUUID();

        willThrow(new UserNotFoundException())
                .given(userService)
                .delete(userId);

        // when & then
        mockMvc.perform(
                        delete("/api/users/{userId}", userId)
                )
                .andExpect(status().isNotFound());

        verify(userService).delete(userId);
    }

    @Test
    @DisplayName("사용자 물리 삭제에 성공하면 204 응답을 반환한다.")
    void hardDeleteUser_whenValidRequest_returnsNoContent() throws Exception {

        // given
        UUID userId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        delete("/api/users/{userId}/hard", userId)
                )
                .andExpect(status().isNoContent());

        verify(userService).hardDelete(userId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 물리 삭제하면 404 응답을 반환한다.")
    void hardDeleteUser_whenUserDoesNotExist_returnsNotFound() throws Exception {

        // given
        UUID userId = UUID.randomUUID();

        willThrow(new UserNotFoundException())
                .given(userService)
                .hardDelete(userId);

        // when & then
        mockMvc.perform(
                        delete("/api/users/{userId}/hard", userId)
                )
                .andExpect(status().isNotFound());

        verify(userService).hardDelete(userId);
    }
}