package com.zb.meeteat.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiControllerAdvice {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Map<String, String> handleValidationExceptions(
      MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    return errors;
  }

  @ExceptionHandler(UserCustomException.class)
  public ResponseEntity<ExceptionResponse> customRequestException(final CustomException c) {
    ErrorCode errorCode = c.getErrorCode();

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(new ExceptionResponse(
            errorCode.getStatus().value(),
            errorCode.getMessage()
        ));
  }

  @Getter
  @AllArgsConstructor
  public static class ExceptionResponse {
    private int status;
    private String message;
  }
}
