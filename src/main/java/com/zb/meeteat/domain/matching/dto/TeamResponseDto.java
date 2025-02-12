package com.zb.meeteat.domain.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamResponseDto {

  private String message;
  private MatchingDto matchingDto;
  //UserDto

}
