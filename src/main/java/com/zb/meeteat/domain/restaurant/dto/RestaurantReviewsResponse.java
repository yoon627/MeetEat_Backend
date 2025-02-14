package com.zb.meeteat.domain.restaurant.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantReviewsResponse {
  private Long id;
  private double rating;
  private String description;
  private String imgUrl;
  private String nickname;
  private String createdAt;
}
