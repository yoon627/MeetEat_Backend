package com.zb.meeteat.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // `@Valid` 유효성 검사 실패 (비밀번호 형식 오류 등)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
    BindingResult bindingResult = e.getBindingResult();

    // 첫 번째 에러만 가져옴 (여러 개가 있을 경우)
    FieldError fieldError = bindingResult.getFieldErrors().get(0);

    // 필드명에 따라 ErrorCode를 매핑
    ErrorCode errorCode = getErrorCodeForField(fieldError.getField());

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("status", errorCode.getStatus().value());
    errorResponse.put("error", errorCode.getCode());
    errorResponse.put("message", errorCode.getMessage());

    return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
  }

  // 필드명에 따라 적절한 ErrorCode를 반환하는 메서드
  private ErrorCode getErrorCodeForField(String field) {
    return switch (field) {
      case "email" -> ErrorCode.INVALID_EMAIL;
      case "password" -> ErrorCode.INVALID_PASSWORD;
      case "nickname" -> ErrorCode.INVALID_NICKNAME;
      default -> ErrorCode.BAD_REQUEST;
    };
  }


  // 예상치 못한 예외 발생 시 500 응답을 반환
  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleGeneralException(Exception e) {
    log.error("서버 오류 발생: {}", e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("서버에 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
  }

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex) {
    ErrorCode errorCode = ex.getErrorCode();

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("status", errorCode.getStatus().value());
    errorResponse.put("error", errorCode.getCode());
    errorResponse.put("message", errorCode.getMessage());

    return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
  }


}
