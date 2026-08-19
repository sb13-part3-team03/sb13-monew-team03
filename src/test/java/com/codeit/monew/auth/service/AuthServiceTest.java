package com.codeit.monew.auth.service;

import com.codeit.monew.auth.dto.request.LoginRequest;
import com.codeit.monew.global.exception.LoginFailedException;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("이메일과 비밀번호가 일치하면 로그인에 성공한다.")
    void login_whenCredentialsAreValid_returnsUser() {

        // given
        LoginRequest request = new LoginRequest(
                "test@example.com",
                "password1234"
        );

        User user = new User(
                "test@example.com",
                "테스트",
                "encoded-password"
        );

        given(userRepository.findByEmail(request.email()))
                .willReturn(Optional.of(user));

        given(passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )).willReturn(true);

        // when
        var response = authService.login(request);

        // then
        assertThat(response.email())
                .isEqualTo("test@example.com");

        assertThat(response.nickname())
                .isEqualTo("테스트");

        verify(passwordEncoder)
                .matches("password1234", "encoded-password");
    }

    @Test
    @DisplayName("존재하지 않는 이메일이면 로그인에 실패한다.")
    void login_whenEmailDoesNotExist_throwsException() {

        // given
        LoginRequest request = new LoginRequest(
                "none@example.com",
                "password1234"
        );

        given(userRepository.findByEmail(request.email()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다.")
    void login_whenPasswordDoesNotMatch_throwsException() {

        // given
        LoginRequest request = new LoginRequest(
                "test@example.com",
                "wrong-password"
        );

        User user = new User(
                "test@example.com",
                "테스트",
                "encoded-password"
        );

        given(userRepository.findByEmail(request.email()))
                .willReturn(Optional.of(user));

        given(passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");

        verify(passwordEncoder)
                .matches("wrong-password", "encoded-password");
    }
}