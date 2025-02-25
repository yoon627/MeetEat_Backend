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
  public ResponseEntity<Map<String, Object>> handleValidationException(
      MethodArgumentNotValidException e) {
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


  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneralException(Exception e) {
    log.error("서버 오류 발생: {}", e.getMessage(), e);

    // CustomException이지만 잘못해서 여기로 들어온 경우 확인
    if (e instanceof CustomException customException) {
      return handleCustomException(customException);
    }

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    errorResponse.put("error", "INTERNAL_SERVER_ERROR");
    errorResponse.put("message", e.getMessage()); // 원래 예외 메시지 유지

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }


  @ExceptionHandler(CustomException.class)
  public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex) {
    log.error(ex.getMessage(), ex);
    log.error(String.valueOf(ex.getErrorCode()), ex);
    ErrorCode errorCode = ex.getErrorCode();
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("status", errorCode.getStatus().value());
    errorResponse.put("error", errorCode.getCode());
    errorResponse.put("message", errorCode.getMessage());

    return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
  }


}
