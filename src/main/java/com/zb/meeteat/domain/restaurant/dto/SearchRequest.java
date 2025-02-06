package com.zb.meeteat.domain.restaurant.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest {
  @NotNull
  private String region;    // 지역

  @Pattern(regexp = "^(한식|중식|일식|양식|전체)$", message = "카테고리는 한식,중식,일식,양식,전체 중 하나여야 합니다.")
  private String categoryName;  // 카테고리명

  @NotNull
  private String placeName;     // 장소 명

  @NotNull
  private double userY;         // 사용자 현재 위도

  @NotNull
  private double userX;         // 사용자 현재 경도

  @Pattern(regexp = "^(DEFAULT|RATING|DISTANCE)$", message = "정렬 값은 DEFAULT, RATING, DISTANCE 중 하나여야 합니다.")
  private String sorted = "DEFAULT";        // RATING : 평점순(내림차순), DISTANCE: 거리순(오름차순)

  @Min(value = 1, message = "페이지는 1이상 가능합니다")
  private int page = 1;             // 현재페이지
  @Min(value = 1, message = "보여질 리스트 수 누락되었습니다")
  private int size = 6;             // 최대 항목순
}
