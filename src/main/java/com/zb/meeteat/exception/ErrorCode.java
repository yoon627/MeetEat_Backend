package com.zb.meeteat.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 잘못되었습니다."),
    EMAIL_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_REGISTERED", "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "NICKNAME_ALREADY_REGISTERED", "이미 사용 중인 닉네임입니다."),
    USER_SCHEDULED_FOR_DELETION(HttpStatus.FORBIDDEN, "USER_SCHEDULED_FOR_DELETION", "해당 계정은 탈퇴 예정 상태입니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
