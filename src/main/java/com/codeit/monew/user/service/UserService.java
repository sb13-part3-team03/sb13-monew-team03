package com.codeit.monew.user.service;

import com.codeit.monew.user.dto.request.UserCreateRequest;
import com.codeit.monew.user.dto.response.UserResponse;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserResponse create(UserCreateRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 이메일입니다."
            );
        }

        Instant now = Instant.now();

        User user = new User(
                request.email(),
                request.nickname(),

                // TODO 비밀번호 암호화 적용
                request.password(),

                now
        );

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                savedUser.getCreatedAt()
        );
    }
}