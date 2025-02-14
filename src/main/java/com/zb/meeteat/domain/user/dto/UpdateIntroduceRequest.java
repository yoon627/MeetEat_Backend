package com.zb.meeteat.domain.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateIntroduceRequest {

  @Size(max = 50, message = "한줄 소개는 50자 이내로 입력해주세요.")
  private String introduce;
}
