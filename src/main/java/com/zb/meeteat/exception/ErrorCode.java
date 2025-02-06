package com.zb.meeteat.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
  ,BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.")
  ,REVIEW_NOT_ALLOWED_FOR_CANCELED_MATCHING(HttpStatus.BAD_REQUEST, "취소된 매칭으로는 후기를 작성할 수 없습니다.")
  ,REVIEW_TIME_NOT_EXCEEDED(HttpStatus.BAD_REQUEST, "후기 작성시간은 매칭 약속 시간 이후 2시간 이후부터 작성가능합니다.")
  ,NOT_EXIST_RESTAURANT(HttpStatus.BAD_REQUEST, "삭제되었거나 존재하지 않는 식당입니다.")
  ,FILE_UPLOAD_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "업로드 파일 최대 갯수는 5장입니다.")
  ,INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 이미지 형식입니다. jpg, jpeg, png 만 허용됩니다.")
  ;

  private final HttpStatus httpStatus;
  private final String message;
}
