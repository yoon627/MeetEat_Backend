package com.zb.meeteat.domain.matching.service;

import com.zb.meeteat.domain.ban.service.BanService;
import com.zb.meeteat.domain.matching.dto.MatchingHistoryDto;
import com.zb.meeteat.domain.matching.dto.MatchingRequestDto;
import com.zb.meeteat.domain.matching.dto.UserMatchingHistoryDto;
import com.zb.meeteat.domain.matching.entity.Matching;
import com.zb.meeteat.domain.matching.entity.MatchingHistory;
import com.zb.meeteat.domain.matching.repository.MatchingHistoryRepository;
import com.zb.meeteat.domain.matching.repository.MatchingRepository;
import com.zb.meeteat.domain.report.service.ReportService;
import com.zb.meeteat.domain.restaurant.entity.RestaurantReview;
import com.zb.meeteat.domain.restaurant.repository.RestaurantReviewRepository;
import com.zb.meeteat.domain.sse.service.SseService;
import com.zb.meeteat.domain.user.entity.User;
import com.zb.meeteat.domain.user.repository.UserRepository;
import com.zb.meeteat.domain.user.service.AuthService;
import com.zb.meeteat.type.MatchingStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingHistoryService {

  private final AuthService authService;
  private final MatchingHistoryRepository matchingHistoryRepository;
  private final UserRepository userRepository;
  private final RestaurantReviewRepository restaurantReviewRepository;
  private final MatchingRepository matchingRepository;
  private final SseService sseService;
  private final BanService banService;
  private final ReportService reportService;

  public Page<MatchingHistoryDto> getMatchingHistory(int page, int size) {
    Long userId = authService.getLoggedInUserId();
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<MatchingHistory> matchingHistories = matchingHistoryRepository.findAllByUserId(userId,
        pageable);

    return matchingHistories.map(matchingHistory -> {
      List<UserMatchingHistoryDto> userList = new ArrayList<>();

      List<MatchingHistory> matchingHistoryList = matchingHistoryRepository.findAllByMatchingId(
          matchingHistory.getMatching().getId());
      Matching matching = matchingRepository.findById(matchingHistory.getMatching().getId())
          .orElseThrow();
      for (MatchingHistory history : matchingHistoryList) {
        User user = userRepository.findById(history.getUserId()).orElseThrow();
        RestaurantReview review = restaurantReviewRepository.findRestaurantReviewByMatchingHistoryId(
            history.getId());
        List<MatchingHistory> historyList = matchingHistoryRepository.findAllByUserIdAndCreatedAtIsAfter(
            user.getId(), LocalDateTime.now().minusYears(1L));
        UserMatchingHistoryDto userDto = UserMatchingHistoryDto.builder()
            .id(history.getUserId())
            .nickname(user.getNickname())
            .introduce(user.getIntroduce())
            .review(review)
            .matchingCount(historyList.size())
            .join(history.getJoin())
            .ban(banService.checkBan(userId, user.getId()))
            .report(reportService.checkReport(userId, user.getId(), history.getMatching().getId()))
            .build();

        userList.add(userDto);
      }

      return MatchingHistoryDto.toDto(matchingHistory, userList, matching);
    });
  }

  public MatchingHistoryDto getRecentMatching() {
    Long userId = authService.getLoggedInUserId();
    MatchingHistory matchingHistory = matchingHistoryRepository.findFirstByUserIdAndStatusAndCreatedAtAfterOrderByCreatedAtDesc(
        userId,
        MatchingStatus.MATCHED,
        LocalDateTime.now().minusHours(1L));
    if (matchingHistory == null) {
      return null;
    }
    log.info("@@@@@@@@@@ matching Id @@@@@@@@@@@@@");
    Matching matching = matchingRepository.findById(matchingHistory.getMatching().getId())
        .orElseThrow();
    log.info("@@@@@@@@@@ matching Id:" + matching.getId());
    log.info("@@@@@@@@@@ matching CreatedAt:" + matching.getCreatedAt());

    matchingHistory.setMatching(matching);
    List<UserMatchingHistoryDto> userList = new ArrayList<>();
    List<MatchingHistory> matchingHistories = matchingHistoryRepository.findAllByMatchingId(
        matchingHistory.getMatching().getId());
    for (MatchingHistory history : matchingHistories) {
      User user = userRepository.findById(history.getUserId()).orElseThrow();
      RestaurantReview review = restaurantReviewRepository.findRestaurantReviewByMatchingHistoryId(
          matchingHistory.getId());
      UserMatchingHistoryDto userDto = UserMatchingHistoryDto.builder().id(history.getUserId())
          .nickname(user.getNickname())
          .join(history.getJoin())
          .introduce(user.getIntroduce()).review(review).build();
      userList.add(userDto);
    }
    return MatchingHistoryDto.toDto(matchingHistory, userList, matching);
  }

  public void saveHistory(Matching matching, List<MatchingRequestDto> team) {
    for (MatchingRequestDto matchingRequestDto : team) {
      MatchingHistory matchingHistory = MatchingHistory.builder().status(MatchingStatus.MATCHED)
          .userId(matchingRequestDto.getUserId())
          .matching(matching)
          .join(true)
          .build();
      matchingHistoryRepository.save(matchingHistory);
    }
  }

  public void cancelMatchingHistory(long matchingId) {
    Long userId = authService.getLoggedInUserId();
    MatchingHistory matchingHistory = matchingHistoryRepository.findByMatchingIdAndUserId(
        matchingId, userId);
    matchingHistory.setJoin(false);
    LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(3L);
    boolean penaltyFlag = false;
    if (matchingHistory.getCreatedAt().isBefore(localDateTime)) {
      //TODO 패널티 제거
//      User user = userRepository.findById(userId).orElseThrow();
//      user.setIsPenalty(true);
//      user.setBannedAt(LocalDateTime.now());
//      user.setBannedEndAt(LocalDateTime.now().plusDays(7L));
      penaltyFlag = true;
    }
    //TODO 다른 사람들도 cancelled처리 해줘야댐
    Matching matching = matchingRepository.findById(matchingHistory.getMatching().getId())
        .orElseThrow();
    if (!penaltyFlag) {
      matching.setStatus(MatchingStatus.CANCELLED);
      //TODO Escape처리를 어떻게 할지
    }
    matchingRepository.save(matching);
    matchingHistory.setStatus(MatchingStatus.CANCELLED);
    matchingHistoryRepository.save(matchingHistory);
    List<UserMatchingHistoryDto> userList = new ArrayList<>();
    List<MatchingHistory> matchingHistoryList = matchingHistoryRepository.findAllByMatchingId(
        matchingHistory.getMatching().getId());
    for (MatchingHistory history : matchingHistoryList) {
      //TODO 리팩터링
      User user = userRepository.findById(history.getUserId()).orElseThrow();
      UserMatchingHistoryDto userDto = UserMatchingHistoryDto.builder()
          .id(user.getId())
          .build();
      userList.add(userDto);
    }
    if (penaltyFlag) {
      sseService.notifyEscapedMatching(matching, userList, userId);
    } else {
      sseService.notifyCancelledMatching(matching, userList, userId);
    }
  }
}
