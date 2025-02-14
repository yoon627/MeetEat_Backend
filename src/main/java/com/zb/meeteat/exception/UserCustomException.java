package com.zb.meeteat.exception;

import lombok.Getter;

@Getter
public class UserCustomException extends RuntimeException {

  private final UserErrorCode userErrorCode;

  public UserCustomException(UserErrorCode userErrorCode) {
    super(userErrorCode.getMessage());
    this.userErrorCode = userErrorCode;
  }
}
