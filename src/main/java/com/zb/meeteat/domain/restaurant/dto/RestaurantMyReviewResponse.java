package com.zb.meeteat.domain.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값을 가진 필드는 JSON에서 제외
public class RestaurantMyReviewResponse {
  private Long id;
  private Integer rating;
  private String description;
  private String imageUrl;
  private String nickName;
  private LocalDateTime createdAt;
}
