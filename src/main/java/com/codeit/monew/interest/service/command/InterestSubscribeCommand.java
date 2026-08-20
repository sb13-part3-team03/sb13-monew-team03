package com.codeit.monew.interest.service.command;

import java.util.UUID;

public record InterestSubscribeCommand(
        UUID interestId,
        UUID userId
) {
}
