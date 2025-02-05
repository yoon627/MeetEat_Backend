package com.zb.meeteat.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 응답을 표준화 하기 위한 클래스
 * 성공 여부, 데이터, 메시지를 포함하여 응답 구조를 일관성 있게 유지
 * @param <T> 응답 데이터의 타입
 */

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    /**
     * API 호출 성공 여부
     */
    private boolean success;
    /**
     * API 응답 데이터 (제네릭 타입)
     */
    private T data;

    /**
     * 성공 또는 실패 메시지
     */
    private String message;

    /**
     * 성공적인 API 응답을 생성하는 정적 메서드
     *
     * @param data      응답 데이터
     * @param message   성공 메시지
     * @param <T>       응답 데이터의 타입
     * @return  성공 응답 객체
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    /**
     * 실패한 API 응답을 생성하는 정적 메서드
     *
     * @param message   실패 메시지
     * @param <T>       응답 데이터의 타입 (null 반환)
     * @return          실패 응답 객체
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
