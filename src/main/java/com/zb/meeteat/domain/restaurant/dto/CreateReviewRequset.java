package com.zb.meeteat.domain.restaurant.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class CreateReviewRequset {

  // todo 토큰추가되면 삭제
  @NotNull
  private Long userId;

  @NotNull(message = "매칭 이력 ID는 필수입니다.")
  private Long matchingHistoryId;

  @Min(value = 1, message = "평점은 최소 1점 이상이어야 합니다.")
  @Max(value = 5, message = "평점은 최대 5점 이하여야 합니다.")
  private int rating;

  @NotNull(message = "후기 작성은 필수입니다.")
  @NotBlank(message = "후기 작성은 필수입니다.")
  private String description;

  private MultipartFile file; // 파일은 필수 값으로 검증하지 않음
}
