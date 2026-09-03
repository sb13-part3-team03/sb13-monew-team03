package com.codeit.monew.interest.service.command;

import java.util.UUID;

public record InterestDeleteCommand(
        UUID interestId
) {
}
