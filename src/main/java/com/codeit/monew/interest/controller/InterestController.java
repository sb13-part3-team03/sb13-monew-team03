package com.codeit.monew.interest.controller;

import com.codeit.monew.interest.dto.request.InterestRegisterRequest;
import com.codeit.monew.interest.dto.request.InterestSearchRequest;
import com.codeit.monew.interest.dto.request.InterestUpdateRequest;
import com.codeit.monew.interest.dto.response.CursorPageResponseInterestDto;
import com.codeit.monew.interest.dto.response.InterestDto;
import com.codeit.monew.interest.dto.response.SubscriptionDto;
import com.codeit.monew.interest.service.InterestService;
import com.codeit.monew.interest.service.command.*;
import com.codeit.monew.interest.service.condition.InterestSearchCondition;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController implements InterestControllerDocs{

    private final InterestService interestService;

    @PostMapping
    public ResponseEntity<InterestDto> createInterest(@Valid @RequestBody InterestRegisterRequest request) {
        InterestRegisterCommand command = request.toCommand();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interestService.createInterest(command));
    }

    @PostMapping("/{interestId}/subscriptions")
    public ResponseEntity<SubscriptionDto> subscribe(
            @PathVariable UUID interestId,
            @RequestHeader("Monew-Request-User-ID") UUID userId
    ) {
        InterestSubscribeCommand command = new InterestSubscribeCommand(interestId, userId);

        return ResponseEntity.ok(interestService.subscribe(command));
    }

    @GetMapping
    public ResponseEntity<CursorPageResponseInterestDto> findInterests(
            @Valid @ModelAttribute InterestSearchRequest request,
            @RequestHeader("Monew-Request-User-ID") UUID userId
    ) {
        InterestSearchCondition condition = request.toCondition(userId);

        return ResponseEntity.ok(interestService.findInterests(condition));
    }

    @PatchMapping("/{interestId}")
    public ResponseEntity<InterestDto> updateInterest(
            @PathVariable UUID interestId,
            @Valid @RequestBody InterestUpdateRequest request
    ) {
        InterestUpdateCommand command = request.toCommand(interestId);

        return ResponseEntity.ok(interestService.updateInterest(command));
    }

    @DeleteMapping("/{interestId}")
    public ResponseEntity<Void> deleteInterest(
            @PathVariable UUID interestId
    ) {
        InterestDeleteCommand command = new InterestDeleteCommand(interestId);

        interestService.deleteInterest(command);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{interestId}/subscriptions")
    public ResponseEntity<Void> unsubscribe(
            @PathVariable UUID interestId,
            @RequestHeader("Monew-Request-User-ID") UUID userId
    ) {
        InterestUnsubscribeCommand command = new InterestUnsubscribeCommand(interestId, userId);

        interestService.unsubscribe(command);

        return ResponseEntity.ok().build();
    }
}
