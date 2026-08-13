package com.codeit.monew.user.service;

import com.codeit.monew.user.dto.request.UserCreateRequest;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입에 성공하면 사용자를 저장한다.")
    void createUser_whenValidRequest_savesUser() {

        // given
        UserCreateRequest request = new UserCreateRequest(
                "test@example.com",
                "테스트",
                "password1234"
        );

        given(userRepository.existsByEmail(request.email()))
                .willReturn(false);

        // when
        userService.create(request);

        // then
        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assertThat(savedUser.getEmail())
                .isEqualTo("test@example.com");

        assertThat(savedUser.getNickname())
                .isEqualTo("테스트");
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
                .isInstanceOf(IllegalArgumentException.class);
    }
}