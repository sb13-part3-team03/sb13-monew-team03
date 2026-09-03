package com.codeit.monew.interest.dto.request;

import com.codeit.monew.interest.service.command.InterestUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(description = "수정할 관심사 정보")
public record InterestUpdateRequest(
        @NotNull
        @Size(min = 1, max = 10)
        List<@NotBlank String> keywords

) {
    public InterestUpdateCommand toCommand(UUID interestId) {
        return new InterestUpdateCommand(
                interestId,
                keywords
        );
    }
}
