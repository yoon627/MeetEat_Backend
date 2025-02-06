package com.zb.meeteat.domain.matching.dto;

import com.zb.meeteat.domain.restaurant.dto.RestaurantDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MatchingResponseDto {

  private long id;
  private String message;
  private RestaurantDto restaurantDto;
}
