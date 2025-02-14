package com.zb.meeteat.domain.restaurant.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest {

  @NotNull(message = "유효하지 않은 지역입니다.")
  private Region region;

  @NotNull(message = "유효하지 않은 카테고리입니다.")
  private Category categoryName;

  @NotNull
  private String placeName;

  @NotNull
  private double userY;

  @NotNull
  private double userX;

  @NotNull(message = "유효하지 않은 정렬입니다.")
  private Sort sort; // RATING : 평점순(내림차순), DISTANCE: 거리순(오름차순)

  @Min(value = 0, message = "페이지는 0이상 가능합니다")
  private int page;
  @Min(value = 1, message = "최소 항목수 1이상 가능합니다")
  private int size;
}
