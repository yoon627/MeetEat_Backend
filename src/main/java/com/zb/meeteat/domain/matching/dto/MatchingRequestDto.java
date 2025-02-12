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

  private long userId;
  private double userLon;
  private double userLat;
  private int groupSize;
  private LocalDateTime matchingStartTime;
  private RestaurantDto restaurantDto;
}
