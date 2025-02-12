package com.zb.meeteat.domain.matching.dto;

import com.zb.meeteat.domain.matching.entity.Matching;
import com.zb.meeteat.domain.restaurant.dto.RestaurantDto;
import com.zb.meeteat.type.MatchingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MatchingDto {

  private int count;
  private MatchingStatus status;
  private RestaurantDto restaurant;
  //List<UserDto>

  public static Matching toEntity(MatchingDto matchingDto) {
    return Matching.builder().count(matchingDto.getCount()).status(matchingDto.getStatus())
        .restaurantId(matchingDto.getRestaurant().getId()).build();
  }
}
