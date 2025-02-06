package com.zb.meeteat.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
  ,BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.")
  ;

  private final HttpStatus httpStatus;
  private final String message;
}
