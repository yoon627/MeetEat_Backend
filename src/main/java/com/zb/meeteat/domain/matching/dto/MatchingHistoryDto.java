package com.zb.meeteat.domain.matching.dto;

import com.zb.meeteat.domain.matching.entity.Matching;
import com.zb.meeteat.domain.matching.entity.MatchingHistory;
import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import com.zb.meeteat.type.MatchingStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@AllArgsConstructor
@Builder
public class MatchingHistoryDto {

  private Long id;
  private Long userId;
  private MatchingStatus matchingStatus;
  private MatchingDto matching;
  private LocalDateTime createdAt;

  public static MatchingHistoryDto toDto(MatchingHistory matchingHistory,
      List<UserMatchingHistoryDto> userList, Matching matching) {
    return MatchingHistoryDto.builder()
        .id(matchingHistory.getId())
        .userId(matchingHistory.getUserId())
        .matchingStatus(matchingHistory.getStatus())
        .matching(MatchingDto.toDto(matching, userList))
        .createdAt(matchingHistory.getCreatedAt()).build();
  }

  public static MatchingHistory toEntity(MatchingHistoryDto matchingHistoryDto,
      Restaurant restaurant) {
    return MatchingHistory.builder()
        .status(MatchingStatus.MATCHED)
        .userId(matchingHistoryDto.getUserId())
        .matching(
            MatchingDto.toEntity(matchingHistoryDto.getMatching(), restaurant))
        .build();
  }
}


