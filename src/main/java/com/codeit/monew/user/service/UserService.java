package com.codeit.monew.user.service;

import com.codeit.monew.global.exception.DuplicateEmailException;
import com.codeit.monew.global.exception.UserNotFoundException;
import com.codeit.monew.user.dto.request.UserCreateRequest;
import com.codeit.monew.user.dto.request.UserUpdateRequest;
import com.codeit.monew.user.dto.response.UserResponse;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse create(UserCreateRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }

        String encodedPassword =
                passwordEncoder.encode(request.password());

        User user = new User(
                request.email(),
                request.nickname(),
                encodedPassword
        );

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                savedUser.getCreatedAt()
        );
    }

    public UserResponse update(
            UUID userId,
            UserUpdateRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.updateNickname(request.nickname());

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt()
        );
    }

    public void delete(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.delete();
    }
}