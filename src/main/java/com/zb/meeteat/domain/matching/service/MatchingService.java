package com.zb.meeteat.domain.matching.service;

import com.zb.meeteat.domain.matching.dto.JoinRequestDto;
import com.zb.meeteat.domain.matching.dto.MatchingDto;
import com.zb.meeteat.domain.matching.dto.MatchingRequestDto;
import com.zb.meeteat.domain.matching.repository.MatchingRepository;
import com.zb.meeteat.domain.redis.service.RedisService;
import com.zb.meeteat.domain.restaurant.dto.RestaurantDto;
import com.zb.meeteat.domain.sse.service.SseService;
import com.zb.meeteat.domain.user.service.AuthService;
import com.zb.meeteat.type.MatchingStatus;
import jakarta.annotation.PostConstruct;
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

  @PostConstruct
  public void initTeamIdQueue() {
    for (int i = 1; i <= 1000000; i++) {
      teamIdPq.add(i);
    }
  }

  public void requestMatching(MatchingRequestDto matchingRequestDto) {
    long userId = authService.getLoggedInUserId();
    log.info("Matching requested: " + userId);
    log.info("매칭 요청 도착: userId={}, matchingRequestDto={}", userId, matchingRequestDto);
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
        redisService.makeTempTeam(teamName, team.size());
        tempTeamMap.put(teamName, team);
        sseService.notifyTempTeam(team, teamId);
        log.info("생성된 임시 팀 사람들에게 알려줌: teamId={}", teamId);
      } else {
        for (MatchingRequestDto member : team) {
          redisService.rightPushMatchingQueue(member);
          log.info("임시 팀 인원이 모자라서 팀 해체");
        }
      }
    }
  }

  public void joinTempTeam(JoinRequestDto joinRequestDto) {
    long userId = authService.getLoggedInUserId();
    String teamName = "team" + joinRequestDto.getTeamId();
    if (redisService.isTempTeamExist(teamName)) {
      if (joinRequestDto.isJoin()) {
        log.info("생성된 임시 팀에 유저가 합류함: teamId={},userId={}", joinRequestDto.getTeamId(), userId);
        redisService.addCurrentTempTeamSize(teamName);
        int count = redisService.getCurrentTempTeamSize(teamName);
        sseService.notifyTempTeamJoin(tempTeamMap.get(teamName), joinRequestDto);
        if (count == redisService.getTotalTempTeamSize(teamName)) {
          makeTeam(tempTeamMap.get(teamName));
          redisService.removeTempTeam(teamName);
        }
      } else {
        List<MatchingRequestDto> team = tempTeamMap.get(teamName);
        teamIdPq.add(joinRequestDto.getTeamId());
        log.info("생성된 임시 팀을 유저가 거절함: teamId={},userId={}", joinRequestDto.getTeamId(), userId);
        sseService.notifyTempTeamJoin(tempTeamMap.get(teamName), joinRequestDto);
      }
    } else {
      //TODO 팀이 해체됐을때 할 로직 추가
      log.info("생성된 임시 팀이 시간이 지나 해체됨: teamId={}", joinRequestDto.getTeamId());
      teamIdPq.add(joinRequestDto.getTeamId());
    }
  }

  public void makeTeam(List<MatchingRequestDto> team) {
    log.info("팀 생성 완료: team={}", team);
    //TODO 식당 선정 로직
    RestaurantDto restaurantDto = team.getFirst().getRestaurantDto();
    log.info("팀 생성 후 선정된 식당: restaurantDto={}", restaurantDto);
    //TODO 식당 저장 로직
    //TODO matchingDto에 식당추가
    MatchingDto matchingDto = MatchingDto.builder().count(team.size())
        .restaurant(restaurantDto)
        .status(MatchingStatus.MATCHED).build();
    saveTeam(matchingDto);
    log.info("팀 생성 알림: team={}", team);
    sseService.notifyTeam(restaurantDto, team);
    log.info("팀 생성 알림 보내기 완료: team={}", team);

    //TODO 매칭내역 저장
  }

  public void saveTeam(MatchingDto matchingDto) {
    matchingRepository.save(MatchingDto.toEntity(matchingDto));
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
//    if (member1.getGroupSize() != member2.getGroupSize()) {
//      return false;
//    }
//    return checkDistanceCondition(member1, member2);
    return true;
  }

  private boolean checkDistanceCondition(MatchingRequestDto member1, MatchingRequestDto member2) {
    if (calculateDistance(member1.getUserLat(), member1.getUserLon(),
        member2.getRestaurantDto().getLat(), member2.getRestaurantDto().getLon())
        > MAX_DISTANCE_CONDITION) {
      return false;
    }
    if (calculateDistance(member2.getUserLat(), member2.getUserLon(),
        member1.getRestaurantDto().getLat(), member1.getRestaurantDto().getLon())
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
