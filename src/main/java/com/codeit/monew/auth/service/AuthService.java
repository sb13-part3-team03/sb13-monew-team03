package com.codeit.monew.auth.service;

import com.codeit.monew.auth.dto.request.LoginRequest;
import com.codeit.monew.user.exception.LoginFailedException;
import com.codeit.monew.user.dto.response.UserResponse;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(LoginFailedException::new);

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new LoginFailedException();
        }

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt()
        );
    }
}