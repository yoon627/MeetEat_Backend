package com.zb.meeteat.domain.restaurant.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantMyReviewResponse {
  private Long id;
  private Integer rating;
  private String description;
  private String imgUrl;
  private String nickName;
  private LocalDateTime createdAt;
}
