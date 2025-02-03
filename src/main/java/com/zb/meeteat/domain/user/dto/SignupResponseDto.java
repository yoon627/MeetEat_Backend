package com.zb.meeteat.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SignupResponseDto {
    private String message;

    @Builder
    public SignupResponseDto(String message) {
        this.message = message;
    }
}
