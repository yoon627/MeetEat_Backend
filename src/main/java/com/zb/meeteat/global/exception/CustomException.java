package com.zb.meeteat.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode; // 에러 코드 관리

    // 생성자에서 에러 메시지와 함께 ErrorCode를 받음
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 부모 클래스(RunTimeException)의 message 초기화
        this.errorCode = errorCode;    // 에러 코드 저장
    }

}
