package com.zb.meeteat.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SignupResponseDto {
    private String email;
    private String nickname;

    @Builder
    public SignupResponseDto(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
    }
}
