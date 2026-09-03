package com.codeit.monew.interest.service.command;

import java.util.List;
import java.util.UUID;

public record InterestUpdateCommand(
        UUID interestId,
        List<String> keywords
) {
}
