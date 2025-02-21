package com.zb.meeteat.domain.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserJoinDto {

  private long id;
  private String nickname;
  private boolean join;
}
