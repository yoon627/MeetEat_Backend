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
    USER_SCHEDULED_FOR_DELETION(HttpStatus.FORBIDDEN, "USER_SCHEDULED_FOR_DELETION", "해당 계정은 탈퇴 예정 상태입니다.")
    ,REVIEW_NOT_ALLOWED_FOR_CANCELED_MATCHING(HttpStatus.BAD_REQUEST, "취소된 매칭으로는 후기를 작성할 수 없습니다.")
    ,REVIEW_TIME_NOT_EXCEEDED(HttpStatus.BAD_REQUEST, "후기 작성시간은 매칭 약속 시간 이후 2시간 이후부터 작성가능합니다.")
    ,CANCELED_MATCHING(HttpStatus.BAD_REQUEST, "취소된 매칭입니다.")
    ,NOT_EXIST_RESTAURANT(HttpStatus.BAD_REQUEST, "삭제되었거나 존재하지 않는 식당입니다.")
    ,FAIL_FILE_UPLOAD(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패하였습니다.")
    ,FILE_UPLOAD_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "업로드 파일 최대 갯수는 5장입니다.")
    ,INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 이미지 형식입니다. jpg, jpeg, png 만 허용됩니다.")
    ,USER_LOCATION_NOT_PROVIDED(HttpStatus.BAD_REQUEST, "거리순 정렬에 사용할 사용자의 위치값이 누락되었습니다.")
    ,BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다.")
    ,INVALID_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "유효하지 않은 토큰입니다.")
    ;


    private final HttpStatus status;
    private final String code;
    private final String message;
}
