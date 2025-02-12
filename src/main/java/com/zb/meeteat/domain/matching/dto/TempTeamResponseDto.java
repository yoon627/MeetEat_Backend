package com.zb.meeteat.domain.matching.dto;

import com.zb.meeteat.domain.restaurant.dto.RestaurantDto;
import java.util.List;
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
public class TempTeamResponseDto {

  private int teamId;
  private String message;
  private List<RestaurantDto> restaurantList;
}
