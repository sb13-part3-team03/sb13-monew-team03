package com.codeit.monew.notification.controller;

import com.codeit.monew.notification.dto.response.CursorPageResponseNotificationDto;
import com.codeit.monew.notification.dto.request.NotificationSearchRequest;
import com.codeit.monew.notification.service.NotificationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationControllerDoc {

  private static final String REQUEST_USER_ID_HEADER = "Monew-Request-User-ID";

  private final NotificationService notificationService;

  // *프론트에서 20개씩 호출함
  @GetMapping
  @Override
  public ResponseEntity<CursorPageResponseNotificationDto> findAllNotConfirmed(
      @Valid @ModelAttribute NotificationSearchRequest request,
      @RequestHeader(REQUEST_USER_ID_HEADER) UUID userId
  ) {
    CursorPageResponseNotificationDto response = notificationService.findAllNotConfirmed(request.toCondition(userId));
    return ResponseEntity.ok(response);
  }

  @PatchMapping
  @Override
  public ResponseEntity<Void> confirmAll(
      @RequestHeader(REQUEST_USER_ID_HEADER) UUID userId
  ) {
    notificationService.confirmAll(userId);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{notificationId}")
  @Override
  public ResponseEntity<Void> confirm(
      @PathVariable UUID notificationId,
      @RequestHeader(REQUEST_USER_ID_HEADER) UUID userId
  ) {
    notificationService.confirm(notificationId, userId);
    return ResponseEntity.ok().build();
  }

}
