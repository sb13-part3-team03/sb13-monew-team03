package com.codeit.monew.notification.entity;

import com.codeit.monew.global.entity.BaseEntity;
import com.codeit.monew.notification.enums.ResourceType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications")
public class Notification extends BaseEntity {

  @NotNull String content;
  @NotNull UUID userId;
  @NotNull ResourceType resourceType;
  @NotNull UUID resourceId;
  @NotNull Boolean confirmed;

  public Notification(String content, UUID userId, ResourceType resourceType, UUID resourceId, boolean confirmed) {
    this.content = content;
    this.userId = userId;
    this.resourceType = resourceType;
    this.resourceId = resourceId;
    this.confirmed = confirmed;
  }

  public void confirm() {
    this.confirmed = true;
  }

}
