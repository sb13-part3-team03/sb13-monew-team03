package com.codeit.monew.interest.dto.request;

import com.codeit.monew.interest.service.command.InterestRegisterCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "관심사 정보")
public record InterestRegisterRequest(
        @NotBlank
        @Size(max = 50)
        String name,

        @NotNull
        @Size(min = 1, max = 10)
        List<@NotBlank String> keywords

) {
    public InterestRegisterCommand toCommand() {
        return new InterestRegisterCommand(
                name,
                keywords
        );
    }
}
