package com.zb.meeteat.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode {
  USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
  INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 잘못되었습니다."),
  EMAIL_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_REGISTERED", "이미 사용 중인 이메일입니다."),
  NICKNAME_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "NICKNAME_ALREADY_REGISTERED",
      "이미 사용 중인 닉네임입니다."),
  USER_SCHEDULED_FOR_DELETION(HttpStatus.FORBIDDEN, "USER_SCHEDULED_FOR_DELETION",
      "해당 계정은 탈퇴 예정 상태입니다."),
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다."),
  INVALID_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
  PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", "현재 비밀번호가 일치하지 않습니다."),
  INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD",
      "비밀번호는 최소 8자 이상이며, 영문, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."),
  SAME_PASSWORD(HttpStatus.BAD_REQUEST, "SAME_PASSWORD", "현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다."),
  USER_CANNOT_BE_DELETED_DUE_TO_MATCHING(HttpStatus.BAD_REQUEST,
      "USER_CANNOT_BE_DELETED_DUE_TO_MATCHING", " 현재 매칭 진행 중이므로 탈퇴할 수 없습니다.");


  private final HttpStatus status;
  private final String code;
  private final String message;
}
