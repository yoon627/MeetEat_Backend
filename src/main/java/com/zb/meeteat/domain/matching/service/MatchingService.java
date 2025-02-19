package com.zb.meeteat.domain.matching.service;

import com.zb.meeteat.domain.matching.dto.JoinRequestDto;
import com.zb.meeteat.domain.matching.dto.MatchingDto;
import com.zb.meeteat.domain.matching.dto.MatchingRequestDto;
import com.zb.meeteat.domain.matching.entity.Matching;
import com.zb.meeteat.domain.matching.repository.MatchingRepository;
import com.zb.meeteat.domain.redis.service.RedisService;
import com.zb.meeteat.domain.restaurant.dto.RestaurantDto;
import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import com.zb.meeteat.domain.restaurant.service.RestaurantService;
import com.zb.meeteat.domain.sse.service.SseService;
import com.zb.meeteat.domain.user.entity.User;
import com.zb.meeteat.domain.user.repository.UserRepository;
import com.zb.meeteat.domain.user.service.AuthService;
import com.zb.meeteat.type.MatchingStatus;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

  private static final double EARTH_RADIUS = 6371000; //단위:m(미터)
  private static final double MAX_DISTANCE_CONDITION = 2000; //단위:m(미터)
  private final AuthService authService;
  private final SseService sseService;
  private final RedisService redisService;
  private final MatchingRepository matchingRepository;
  private final int MAX_SEARCH_COUNT = 100;
  private final Set<Long> cancelledUserSet = ConcurrentHashMap.newKeySet();//취소한 유저가 계산되고 있는 경우를 체크하기 위해
  private final PriorityQueue<Integer> teamIdPq = new PriorityQueue<>();
  private final Map<String, List<MatchingRequestDto>> tempTeamMap = new ConcurrentHashMap<>();
  private final Map<String, Integer> tmpTeamAgreeCountMap = new ConcurrentHashMap<>();
  private final Map<String, Integer> tmpTeamResponseCountMap = new ConcurrentHashMap<>();
  private final RestaurantService restaurantService;
  private final MatchingHistoryService matchingHistoryService;
  private final UserRepository userRepository;
  private final Map<String, LocalDateTime> tempTeamTimeMap = new ConcurrentHashMap<>();

  @PostConstruct
  public void initTeamIdQueue() {
    for (int i = 1; i <= 1000000; i++) {
      teamIdPq.add(i);
    }
  }

//  public void saveMatching(MatchingDto matchingDto) {
//    log.info("Save matching " + matchingDto);
//    log.info(matchingDto.toString());
//    log.info(String.valueOf(matchingDto.getCount()));
//    log.info(String.valueOf(matchingDto.getStatus()));
//    log.info(matchingDto.getRestaurant().toString());
//    log.info(matchingDto.getRestaurant().getId().toString());
//
//    matchingRepository.save(MatchingDto.toEntity(matchingDto));
//  }

  public void requestMatching(MatchingRequestDto matchingRequestDto) {
    long userId = authService.getLoggedInUserId();
    User user = userRepository.findById(userId).orElseThrow(RuntimeException::new);
    if (user.getBannedEndAt() != null && user.getBannedEndAt().isAfter(LocalDateTime.now())) {
      //TODO 밴되었다고 알려주기
      return;
    }
    cancelledUserSet.remove(userId);
    log.info("Matching requested: " + userId);
    log.info("매칭 요청 도착: userId={}, matchingRequestDto={}", userId, matchingRequestDto);
    log.info("matchingRequestDto.place={}", matchingRequestDto.getPlace());
    log.info("matchingRequestDto.placeName={}", matchingRequestDto.getPlace().getName());
    matchingRequestDto.setUserId(userId);
    redisService.addMatchingQueue(matchingRequestDto);
    log.info("매칭 요청 처리 완료: userId={}, matchingRequestDto={}", userId, matchingRequestDto);
  }

  public void cancelMatching() {
    long userId = authService.getLoggedInUserId();
    log.info("매칭 취소 도착: userId={}", userId);
    sseService.unsubscribe(userId);
    cancelledUserSet.add(userId);
    log.info("매칭 취소 처리 완료: userId={}", userId);
  }

  @Scheduled(fixedDelay = 1000) // 1초마다 실행
  public void makeTempTeam() {
    while (!redisService.isMatchingQueueEmpty()) {
      MatchingRequestDto user = redisService.leftPopMatchingQueue();
      List<MatchingRequestDto> team = new LinkedList<>();
      team.add(user);
      log.info("임시 팀 생성 시작: 기준 userId={}", user.getUserId());
      int cnt = 0;
      while (!redisService.isMatchingQueueEmpty() && cnt++ < MAX_SEARCH_COUNT
          && team.size() < user.getGroupSize()) {
        MatchingRequestDto candidate = redisService.leftPopMatchingQueue();
        log.info("임시 팀 생성 중: 기준 userId={}, candidate={}", user.getUserId(), candidate.getUserId());
        if (cancelledUserSet.contains(candidate.getUserId())) {
          cancelledUserSet.remove(candidate.getUserId());
          log.info("임시 팀 생성 중: 계산 중 매칭 취소한 유저 삭제, candidate={}", candidate.getUserId());
          continue;
        }
        if (checkTeamCondition(team, candidate)) {
          team.add(candidate);
          log.info("임시 팀 생성 중: 후보군 팀에 합류, candidate={}", candidate.getUserId());
        } else {
          redisService.rightPushMatchingQueue(candidate);
          log.info("임시 팀 생성 중: 후보군 팀에서 제외, candidate={}", candidate.getUserId());
        }
        if (cancelledUserSet.contains(user.getUserId())) {
          break;
        }
      }
      if (team.size() == user.getGroupSize()) {
        log.info("임시 팀 생성 완료 후 확인: 계산 중에 취소한 인원이 있는지 확인 중");
        for (MatchingRequestDto candidate : team) {
          if (cancelledUserSet.contains(candidate.getUserId())) {
            for (MatchingRequestDto cancelled : team) {
              if (!cancelledUserSet.contains(cancelled.getUserId())) {
                redisService.rightPushMatchingQueue(cancelled);
              }
            }
            log.info("임시 팀 생성 완료 후 확인: 계산 중에 취소한 인원이 있어 나머지 인원 다시 매칭 큐에 넣어줌");
            cancelledUserSet.remove(candidate.getUserId());
            break;
          }
        }
        log.info("임시 팀 생성 완료: 계산 중에 취소한 인원이 없음");
        int teamId = teamIdPq.poll();
        String teamName = "team" + teamId;
//        redisService.makeTempTeam(teamName, team.size());
        tempTeamTimeMap.put(teamName, LocalDateTime.now());
        tmpTeamAgreeCountMap.put(teamName, 0);
        tmpTeamResponseCountMap.put(teamName, 0);
        tempTeamMap.put(teamName, team);
        sseService.notifyTempTeam(team, teamId);
        log.info("생성된 임시 팀 사람들에게 알려줌: teamId={}", teamId);
      } else {
        log.info("임시 팀 인원이 모자라서 팀 해체");
        for (MatchingRequestDto member : team) {
          if (!cancelledUserSet.contains(member.getUserId())) {
            redisService.rightPushMatchingQueue(member);
          }
        }
      }
    }
  }

  public void joinTempTeam(JoinRequestDto joinRequestDto) {
    long userId = authService.getLoggedInUserId();
    joinRequestDto.setUserId(userId);
    String teamName = "team" + joinRequestDto.getTeamId();
    log.info("join 신청이 서비스에 옴");
    if (tempTeamTimeMap.get(teamName).isAfter(LocalDateTime.now().minusMinutes(1))) {
      log.info("join 신청이 시간안에 옴");
//    if (redisService.isTempTeamExist(teamName)) {
      log.info(joinRequestDto.toString());
      log.info(String.valueOf(joinRequestDto.isJoin()));
      if (joinRequestDto.isJoin()) {
        log.info("생성된 임시 팀에 유저가 합류함: teamId={},userId={}", joinRequestDto.getTeamId(), userId);
//        redisService.addCurrentTempTeamSize(teamName);
        tmpTeamAgreeCountMap.put(teamName, tmpTeamAgreeCountMap.get(teamName) + 1);
        tmpTeamResponseCountMap.put(teamName, tmpTeamResponseCountMap.get(teamName) + 1);
        int agreeCount = tmpTeamAgreeCountMap.get(teamName);
        log.info("join 알림 보내기 전");
        log.info("agreeCount={}, teamSize={}", agreeCount, tempTeamMap.get(teamName).size());
        sseService.notifyTempTeamJoin(tempTeamMap.get(teamName), joinRequestDto);
        log.info("join 알림 보내기 완료");
        if (agreeCount == tempTeamMap.get(teamName).size()) {
          log.info("임시팀 모두가 동의해 팀 생성");
          makeTeam(tempTeamMap.get(teamName));
//          redisService.removeTempTeam(teamName);
          teamIdPq.add(joinRequestDto.getTeamId());
          return;
        }
        if (tmpTeamResponseCountMap.get(teamName) == tempTeamMap.get(teamName).size()) {
          List<MatchingRequestDto> team = tempTeamMap.get(teamName);
          for (MatchingRequestDto member : team) {
            if (!cancelledUserSet.contains(member.getUserId())) {
              redisService.rightPushMatchingQueue(member);
            }
          }
          log.info("생성된 임시 팀이 모두 동의하지 않아 해체됨: teamId={}", joinRequestDto.getTeamId());
          teamIdPq.add(joinRequestDto.getTeamId());
          return;
        }
      } else {
        tmpTeamResponseCountMap.put(teamName, tmpTeamResponseCountMap.get(teamName) + 1);
        log.info("생성된 임시 팀을 유저가 거절함: teamId={},userId={}", joinRequestDto.getTeamId(), userId);
        if (tmpTeamResponseCountMap.get(teamName) == tempTeamMap.get(teamName).size()) {
          teamIdPq.add(joinRequestDto.getTeamId());
          List<MatchingRequestDto> team = tempTeamMap.get(teamName);
          sseService.notifyTempTeamJoin(team, joinRequestDto);
          for (MatchingRequestDto member : team) {
            if (!cancelledUserSet.contains(member.getUserId())) {
              redisService.rightPushMatchingQueue(member);
            }
          }
          log.info("생성된 임시 팀이 모두 동의하지 않아 해체됨: teamId={}", joinRequestDto.getTeamId());
          teamIdPq.add(joinRequestDto.getTeamId());
        }
      }
    } else {
      List<MatchingRequestDto> team = tempTeamMap.get(teamName);
      for (MatchingRequestDto member : team) {
        if (!cancelledUserSet.contains(member.getUserId())) {
          redisService.rightPushMatchingQueue(member);
        }
      }
      log.info("생성된 임시 팀이 시간이 지나 해체됨: teamId={}", joinRequestDto.getTeamId());
      teamIdPq.add(joinRequestDto.getTeamId());
    }
  }

  @Transactional
  public void makeTeam(List<MatchingRequestDto> team) {
    log.info("팀 생성 완료: team={}", team);
    RestaurantDto restaurantDto = team.getFirst().getPlace();
    log.info("팀 생성 후 선정된 식당: restaurantDto={}", restaurantDto);
    Restaurant restaurant = restaurantService.saveRestaurant(restaurantDto);
    log.info("Restaurant Id after Saving " + restaurant.getId());
    restaurantDto.setId(restaurant.getId());
    restaurantDto = RestaurantDto.toDto(restaurant);
    MatchingDto matchingDto = MatchingDto.builder()
        .count(team.size())
        .restaurant(restaurantDto)
        .status(MatchingStatus.MATCHED).build();
    Matching matching = saveTeam(matchingDto, team, restaurant);
    log.info("팀 생성 알림: team={}", team);
    sseService.notifyTeam(restaurantDto, team, matching);
    log.info("팀 생성 알림 보내기 완료: team={}", team);
//    saveMatching(matchingDto);
    //TODO 3분이내 이탈자 발생
  }

  public Matching saveTeam(MatchingDto matchingDto, List<MatchingRequestDto> team,
      Restaurant restaurant) {
//    log.info("MatchingDto Restaurant Id:" + String.valueOf(matchingDto.getRestaurant().getId()));
//    if (matchingDto.getRestaurant() != null && matchingDto.getRestaurant().getId() == null) {
//      Restaurant restaurant = restaurantService.saveRestaurant(matchingDto.getRestaurant());
//      matchingDto.getRestaurant().setId(restaurant.getId());
//    }
    log.info("Save matching " + matchingDto);
    log.info(matchingDto.toString());
    log.info(String.valueOf(matchingDto.getCount()));
    log.info(String.valueOf(matchingDto.getStatus()));
    log.info(matchingDto.getRestaurant().toString());
    log.info(matchingDto.getRestaurant().getId().toString());
    //TODO 문제 발생
    Matching org = MatchingDto.toEntity(matchingDto, restaurant);
    log.info("org count:" + org.getCount());
    log.info("org status:" + org.getStatus());
    log.info("org restaurant:" + org.getRestaurant().toString());
    log.info("org restaurantId:" + org.getRestaurant().getId().toString());
    log.info("org createdAt:" + org.getCreatedAt());
    Matching matching = matchingRepository.save(org);
    log.info("@@@@@@@@@ MATCHING SAVE SUCCESS @@@@@@@@@@@");
    matchingHistoryService.saveHistory(matching, team);
    log.info("@@@@@@@@@ MATCHING HISTORY SAVE SUCCESS @@@@@@@@@@@");
    return matching;
  }

  private boolean checkTeamCondition(List<MatchingRequestDto> team, MatchingRequestDto member) {
    for (MatchingRequestDto m : team) {
      if (!checkCondition(m, member)) {
        return false;
      }
    }
    return true;
  }

  private boolean checkCondition(MatchingRequestDto member1, MatchingRequestDto member2) {
    //TODO 테스트용으로 모두 허용
//    if (member1.getGroupSize() != member2.getGroupSize()) {
//      return false;
//    }
//    return checkDistanceCondition(member1, member2);
    return true;
  }

  private boolean checkDistanceCondition(MatchingRequestDto member1, MatchingRequestDto member2) {
    if (calculateDistance(member1.getUserLat(), member1.getUserLon(),
        member2.getPlace().getLat(), member2.getPlace().getLon())
        > MAX_DISTANCE_CONDITION) {
      return false;
    }
    if (calculateDistance(member2.getUserLat(), member2.getUserLon(),
        member1.getPlace().getLat(), member1.getPlace().getLon())
        > MAX_DISTANCE_CONDITION) {
      return false;
    }
    return true;
  }

  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double radLat1 = Math.toRadians(lat1);
    double radLat2 = Math.toRadians(lat2);
    double a = Math.pow(Math.sin(dLat / 2), 2) +
        Math.pow(Math.sin(dLon / 2), 2) * Math.cos(radLat1) * Math.cos(radLat2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS * c;
  }

}
