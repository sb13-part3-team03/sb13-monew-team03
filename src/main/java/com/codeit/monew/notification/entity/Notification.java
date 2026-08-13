package com.codeit.monew.notification.entity;

import com.codeit.monew.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications")
public class Notification extends BaseEntity {
  String content;
  String userId;
  String resourceType;

  public Notification(String content, String userId, String resourceType, String resourceId,
      boolean confirmed) {
    this.content = content;
    this.userId = userId;
    this.resourceType = resourceType;
    this.resourceId = resourceId;
    this.confirmed = confirmed;
  }

  String resourceId;
  boolean confirmed;
}
