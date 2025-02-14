package com.zb.meeteat.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

  @NotBlank(message = "현재 비밀번호는 필수 입력 항목입니다.")
  private String currentPassword;

  @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.")
  @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[\\p{Punct}\\p{S}&&[^\\p{So}]]).{8,}$",
      message = "비밀번호는 최소 8자 이상이며, 영문, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."
  )
  private String newPassword;
}