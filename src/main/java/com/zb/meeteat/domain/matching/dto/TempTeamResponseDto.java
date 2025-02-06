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

  int teamId;
  String message;
  List<RestaurantDto> restaurantList;
}
