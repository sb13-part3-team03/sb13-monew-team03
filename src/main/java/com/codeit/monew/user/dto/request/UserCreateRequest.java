package com.codeit.monew.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
        String email,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
        String nickname,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}