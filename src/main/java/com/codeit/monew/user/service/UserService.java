package com.codeit.monew.user.service;

import com.codeit.monew.user.exception.DuplicateEmailException;
import com.codeit.monew.user.exception.UserNotFoundException;
import com.codeit.monew.user.dto.request.UserCreateRequest;
import com.codeit.monew.user.dto.request.UserUpdateRequest;
import com.codeit.monew.user.dto.response.UserResponse;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import com.codeit.monew.useractivity.event.UserActivityEventPublisher;
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
    private final UserActivityEventPublisher activityEvents;

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
        activityEvents.profileChanged(savedUser);

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
        activityEvents.profileChanged(user);

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt()
        );
    }

    // 사용자 논리 삭제
    public void delete(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.delete();
    }

    // 사용자 물리 삭제
    public void hardDelete(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        userRepository.delete(user);
        activityEvents.userRemoved(userId);
    }
}
