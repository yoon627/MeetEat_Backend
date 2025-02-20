package com.zb.meeteat.domain.matching.dto;

import com.zb.meeteat.domain.restaurant.entity.RestaurantReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserMatchingHistoryDto {

  private Long id;
  private String nickname;
  private String introduce;
  private RestaurantReview review;
  private Integer matchingCount;
  private Boolean ban;
  private Boolean report;
  private Boolean join;
}
