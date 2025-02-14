package com.zb.meeteat.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final UserErrorCode userErrorCode;

    public CustomException(UserErrorCode userErrorCode) {
        super(userErrorCode.getMessage());
        this.userErrorCode = userErrorCode;
    }
}
