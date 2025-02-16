package com.zb.meeteat.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthCodeResponseDto {

  private final String accessToken;
  private final Boolean needProfileUpdate;

  // 첫 로그인 시 사용하는 생성자
  public AuthCodeResponseDto(String accessToken, boolean needProfileUpdate) {
    this.accessToken = accessToken;
    this.needProfileUpdate = needProfileUpdate;
  }

  // 이후 로그인 시 사용하는 생성자
  public AuthCodeResponseDto(String accessToken) {
    this.accessToken = accessToken;
    this.needProfileUpdate = null;
  }

}
