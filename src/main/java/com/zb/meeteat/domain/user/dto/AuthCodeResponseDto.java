package com.zb.meeteat.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthCodeResponseDto {
    private String accessToken;
    private boolean needProfileUpdate;

}
