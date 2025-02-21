package com.zb.meeteat.domain.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@AllArgsConstructor
@Builder
public class JoinRequestDto {

  private int teamId;
  private long userId;
  private boolean join;

}
