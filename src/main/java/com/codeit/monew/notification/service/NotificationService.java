package com.codeit.monew.notification.service;

import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.global.exception.MonewException;
import com.codeit.monew.notification.dto.response.CursorPageResponseNotificationDto;
import com.codeit.monew.notification.dto.response.NotificationDto;
import com.codeit.monew.notification.condition.NotificationSearchCondition;
import com.codeit.monew.notification.entity.Notification;
import com.codeit.monew.notification.enums.ResourceType;
import com.codeit.monew.notification.event.NotificationCreateEvent;
import com.codeit.monew.notification.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public CursorPageResponseNotificationDto findAllNotConfirmed(NotificationSearchCondition condition) {
    int limit = condition.limit();
    UUID cursor = condition.cursor() == null
        ? null
        : UUID.fromString(condition.cursor());

    List<Notification> notifications = notificationRepository.findAllNotConfirmed(
        condition.userId(),
        condition.after(),
        cursor,
        limit + 1
    );

    boolean hasNext = notifications.size() > limit;
    List<Notification> pageContent = hasNext
        ? notifications.subList(0, limit)
        : notifications;

    List<NotificationDto> content = pageContent.stream()
        .map(NotificationDto::from)
        .toList();

    String nextCursor = null;
    Instant nextAfter = null;
    if (hasNext && !pageContent.isEmpty()) {
      Notification lastNotification = pageContent.get(pageContent.size() - 1);
      nextCursor = lastNotification.getId().toString();
      nextAfter = lastNotification.getCreatedAt();
    }

    long totalElements =
        notificationRepository.countByUserIdAndConfirmedFalse(condition.userId());

    return new CursorPageResponseNotificationDto(
        content,
        nextCursor,
        nextAfter,
        content.size(),
        totalElements,
        hasNext
    );
  }

  @Transactional
  public void create(String content, UUID userId, ResourceType resourceType, UUID resourceId) {
    Notification notification = new Notification(content, userId, resourceType, resourceId, false);
    notificationRepository.save(notification);
  }

  public void publishCreateEvent(
      String content,
      UUID userId,
      ResourceType resourceType,
      UUID resourceId
  ) {
    eventPublisher.publishEvent(
        new NotificationCreateEvent(content, userId, resourceType, resourceId)
    );
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createInNewTransaction(
      String content,
      UUID userId,
      ResourceType resourceType,
      UUID resourceId
  ) {
    Notification notification = new Notification(content, userId, resourceType, resourceId, false);
    notificationRepository.save(notification);
  }


  @Transactional
  public void confirmAll(UUID userId) {
    List<Notification> notifications =
        notificationRepository.findByUserIdAndConfirmedFalse(userId);
    notifications.forEach(Notification::confirm);
    notificationRepository.saveAll(notifications);
  }

  public void confirm(UUID notificationId, UUID userId) {
    Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
        .orElseThrow(() -> new MonewException(ErrorCode.NOTIFICATION_NOT_FOUND));
    notification.confirm();
    notificationRepository.save(notification);
  }

}
