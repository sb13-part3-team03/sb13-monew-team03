package com.codeit.monew.notification.listener;

import com.codeit.monew.notification.event.NotificationCreateEvent;
import com.codeit.monew.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCreateEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationCreateEvent event) {
        try {
            notificationService.createInNewTransaction(
                    event.content(),
                    event.userId(),
                    event.resourceType(),
                    event.resourceId()
            );
        } catch (Exception e) {
            log.error(
                    "알림 생성 실패. userId={}, resourceType={}, resourceId={}",
                    event.userId(), event.resourceType(), event.resourceId(), e
            );
        }
    }
}
