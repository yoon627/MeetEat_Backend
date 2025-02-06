package com.zb.meeteat.domain.matching.dto;

import com.zb.meeteat.domain.restaurant.dto.RestaurantDto;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MatchingRequestDto {

  long userId;
  double userLon;
  double userLat;
  int groupSize;
  LocalDateTime matchingStartTime;
  RestaurantDto restaurantDto;
}
