package com.codeit.monew.interest.service.command;

import java.util.List;

public record InterestRegisterCommand(
        String name,
        List<String> keywords
) {
}
