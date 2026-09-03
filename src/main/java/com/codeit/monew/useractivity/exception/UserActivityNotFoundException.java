package com.codeit.monew.useractivity.exception;

import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.global.exception.MonewException;

public class UserActivityNotFoundException extends MonewException {

  public UserActivityNotFoundException() {
    super(ErrorCode.USER_ACTIVITY_NOT_FOUND);
  }
}
