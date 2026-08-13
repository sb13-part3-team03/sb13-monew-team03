package com.codeit.monew.interest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InterestRegisterRequest(
        @NotBlank
        @Size(max = 50)
        String name,

        @NotNull
        @Size(min = 1, max = 10)
        List<@NotBlank String> keywords

) {
}
