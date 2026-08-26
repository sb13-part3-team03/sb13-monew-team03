package com.codeit.monew.useractivity.controller;

import com.codeit.monew.useractivity.dto.response.UserActivityDto;
import com.codeit.monew.useractivity.service.UserActivityService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-activities")
@RequiredArgsConstructor
public class UserActivityController {

  private final UserActivityService userActivityService;

  @GetMapping("/{userId}")
  public ResponseEntity<UserActivityDto> find(@PathVariable UUID userId) {
    return ResponseEntity.ok(userActivityService.find(userId));
  }
}
