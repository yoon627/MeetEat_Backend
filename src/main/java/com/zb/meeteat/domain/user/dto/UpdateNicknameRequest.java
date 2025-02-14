package com.zb.meeteat.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UpdateNicknameRequest {

  @NotBlank(message = "닉네임은 비워둘 수 없습니다.")
  @Pattern(regexp = "^[^\\s]+$", message = "닉네임에는 공백이 포함될 수 없습니다.")
  private String nickname;
}
