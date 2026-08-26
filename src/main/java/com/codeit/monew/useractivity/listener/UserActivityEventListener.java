package com.codeit.monew.useractivity.listener;

import com.codeit.monew.useractivity.event.UserActivityEvent;
import com.codeit.monew.useractivity.service.UserActivityProjectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityEventListener {
  private final UserActivityProjectionService projection;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(UserActivityEvent event) {
    try {
      projection.apply(event);
    } catch (Exception e) {
      // Like notifications, failure must not turn an already committed operation into an API error.
      // Durable delivery/reconciliation needs an outbox; application events alone do not provide it.
      log.error("User activity projection failed. eventType={}",
          event.getClass().getSimpleName(), e);
    }
  }
}
