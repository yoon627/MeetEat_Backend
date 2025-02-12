package com.zb.meeteat.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthCodeRequestDto {

    @NotBlank(message = "authCode는 필수 입력 항목입니다.")
    private String authCode;

    public AuthCodeRequestDto(String authCode) {
        this.authCode = authCode;
    }
}
