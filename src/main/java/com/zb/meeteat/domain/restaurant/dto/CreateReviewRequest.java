package com.zb.meeteat.domain.restaurant.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CreateReviewRequest {

  @NotNull(message = "매칭 이력 ID는 필수입니다.")
  private Long matchingHistoryId;

  @NotNull(message = "평점을 입력해주세요")
  @Min(value = 1, message = "평점은 최소 1점 이상이어야 합니다.")
  private int rating;

  @NotBlank(message = "후기 작성은 필수입니다.")
  private String description;

  private List<MultipartFile> files;
}
