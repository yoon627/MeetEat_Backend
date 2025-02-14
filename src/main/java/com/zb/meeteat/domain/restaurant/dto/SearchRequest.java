package com.zb.meeteat.domain.restaurant.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

  @NotNull
  @Pattern(regexp = "^(서울|부산|대구|인천|광주|대전|울산|세종|경기|강원|충북|충남|전북|전남|경북|경남|제주)$",
      message = "지역는 서울,부산,대구,인천,광주,대전,울산,세종,경기,강원,충북,충남,전북,전남,경북,경남,제주 만 검색 가능합니다.")
  private String region;

  @Pattern(regexp = "^(한식|중식|일식|양식|전체)$", message = "카테고리는 한식,중식,일식,양식,전체 중 하나여야 합니다.")
  private String categoryName;

  @NotNull
  private String placeName;

  @NotNull
  private double userY;

  @NotNull
  private double userX;

  @Pattern(regexp = "^(DEFAULT|RATING|DISTANCE)$", message = "정렬 값은 DEFAULT, RATING, DISTANCE 중 하나여야 합니다.")
  private String sorted;       // RATING : 평점순(내림차순), DISTANCE: 거리순(오름차순)

  @Min(value = 0, message = "페이지는 0이상 가능합니다")
  private int page;
  @Min(value = 1, message = "최소 항목수 1이상 가능합니다")
  private int size;
}
