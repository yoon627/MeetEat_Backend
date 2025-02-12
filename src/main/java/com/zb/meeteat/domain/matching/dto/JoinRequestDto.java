package com.zb.meeteat.domain.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class JoinRequestDto {

  private int teamId;
  private long userId;
  private boolean join;

}
