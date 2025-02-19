package com.zb.meeteat.domain.matching.dto;

import com.zb.meeteat.domain.matching.entity.Matching;
import com.zb.meeteat.domain.restaurant.dto.RestaurantDto;
import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import com.zb.meeteat.type.MatchingStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
@Getter
@Setter
@AllArgsConstructor
@Builder
public class MatchingDto {

  private int count;
  private MatchingStatus status;
  private RestaurantDto restaurant;
  private List<UserMatchingHistoryDto> userList;

  public static Matching toEntity(MatchingDto matchingDto, Restaurant restaurant) {
    log.info("MatchingDto.toEntity: count:" + matchingDto.getCount());
    log.info("MatchingDto.toEntity: status:" + matchingDto.getStatus());
    log.info("MatchingDto.toEntity: restaurant Id:" + restaurant.getId());
    return Matching.builder().count(matchingDto.getCount()).status(matchingDto.getStatus())
        .restaurant(restaurant).createdAt(LocalDateTime.now()).build();
  }

  public static MatchingDto toDto(Matching entity, List<UserMatchingHistoryDto> userList) {
    return MatchingDto.builder().count(entity.getCount()).status(entity.getStatus())
        .restaurant(RestaurantDto.toDto(entity.getRestaurant())).userList(userList).build();
  }
}
