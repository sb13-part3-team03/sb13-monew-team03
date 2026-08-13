package com.codeit.monew.notification.enums;

public enum ResourceType {
  INTEREST("관심사"),
  COMMNET("댓글");

  private final String tag;

  ResourceType(String tag) {
    this.tag = tag;
  }
}
