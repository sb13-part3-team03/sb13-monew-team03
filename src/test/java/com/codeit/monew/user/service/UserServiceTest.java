package com.codeit.monew.user.service;

import com.codeit.monew.global.exception.DuplicateEmailException;
import com.codeit.monew.global.exception.UserNotFoundException;
import com.codeit.monew.user.dto.request.UserCreateRequest;
import com.codeit.monew.user.dto.request.UserUpdateRequest;
import com.codeit.monew.user.dto.response.UserResponse;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입에 성공하면 비밀번호를 해시하여 사용자를 저장한다.")
    void createUser_whenValidRequest_savesUserWithEncodedPassword() {

        // given
        UserCreateRequest request = new UserCreateRequest(
                "test@example.com",
                "테스트",
                "password1234"
        );

        given(userRepository.existsByEmail(request.email()))
                .willReturn(false);

        given(passwordEncoder.encode(request.password()))
                .willReturn("encoded-password");

        given(userRepository.save(any(User.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        userService.create(request);

        // then
        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(passwordEncoder)
                .encode("password1234");

        verify(userRepository)
                .save(captor.capture());

        User savedUser = captor.getValue();

        assertThat(savedUser.getEmail())
                .isEqualTo("test@example.com");

        assertThat(savedUser.getNickname())
                .isEqualTo("테스트");

        assertThat(savedUser.getPassword())
                .isEqualTo("encoded-password");

        assertThat(savedUser.getPassword())
                .isNotEqualTo("password1234");
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입하면 예외가 발생한다.")
    void createUser_whenEmailAlreadyExists_throwsException() {

        // given
        UserCreateRequest request = new UserCreateRequest(
                "test@example.com",
                "테스트",
                "password1234"
        );

        given(userRepository.existsByEmail(request.email()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");

        verify(passwordEncoder, never())
                .encode(any());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    @DisplayName("존재하는 사용자의 닉네임을 수정한다.")
    void updateUser_whenUserExists_updatesNickname() {

        // given
        UUID userId = UUID.randomUUID();

        User user = new User(
                "test@example.com",
                "기존닉네임",
                "encoded-password"
        );

        UserUpdateRequest request =
                new UserUpdateRequest("새닉네임");

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));

        // when
        UserResponse response =
                userService.update(userId, request);

        // then
        assertThat(response.nickname())
                .isEqualTo("새닉네임");

        assertThat(user.getNickname())
                .isEqualTo("새닉네임");
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 수정하면 예외가 발생한다.")
    void updateUser_whenUserDoesNotExist_throwsException() {

        // given
        UUID userId = UUID.randomUUID();

        UserUpdateRequest request =
                new UserUpdateRequest("새닉네임");

        given(userRepository.findById(userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                userService.update(userId, request)
        )
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("존재하는 사용자를 논리 삭제하면 deletedAt이 설정된다.")
    void deleteUser_whenUserExists_setsDeletedAt() {

        // given
        UUID userId = UUID.randomUUID();

        User user = new User(
                "test@example.com",
                "테스트",
                "encoded-password"
        );

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));

        // when
        userService.delete(userId);

        // then
        assertThat(user.getDeletedAt())
                .isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 논리 삭제하면 예외가 발생한다.")
    void deleteUser_whenUserDoesNotExist_throwsUserNotFoundException() {

        // given
        UUID userId = UUID.randomUUID();

        given(userRepository.findById(userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.delete(userId))
                .isInstanceOf(UserNotFoundException.class);
    }
}