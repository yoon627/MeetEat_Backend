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
  private final int MAX_SEARCHING_TIME = 100;
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
    long userId = authService.getLoginUserId();
    matchingRequestDto.setUserId(userId);
    redisService.addMatchingQueue(matchingRequestDto);
  }

  public void cancelMatching() {
    long userId = authService.getLoginUserId();
    sseService.unsubscribe(userId);
    cancelledUserSet.add(userId);
  }

  @Scheduled(fixedDelay = 1000) // 1초마다 실행
  public void makeTempTeam() {
    while (!redisService.isMatchingQueueEmpty()) {
      MatchingRequestDto user = redisService.leftPopMatchingQueue();
      List<MatchingRequestDto> team = new LinkedList<>();
      team.add(user);
      int cnt = 0;
      while (redisService.isMatchingQueueEmpty() && cnt++ < MAX_SEARCHING_TIME
          || team.size() < user.getGroupSize()) {
        MatchingRequestDto candidate = redisService.leftPopMatchingQueue();
        if (cancelledUserSet.contains(candidate.getUserId())) {
          cancelledUserSet.remove(candidate.getUserId());
          continue;
        }
        if (checkTeamCondition(team, candidate)) {
          team.add(candidate);
        } else {
          redisService.rightPushMatchingQueue(candidate);
        }
      }
      if (team.size() == user.getGroupSize()) {
        for (MatchingRequestDto candidate : team) {
          if (cancelledUserSet.contains(candidate.getUserId())) {
            for (MatchingRequestDto cancelled : team) {
              if (!cancelledUserSet.contains(cancelled.getUserId())) {
                redisService.rightPushMatchingQueue(cancelled);
              }
            }
            cancelledUserSet.remove(candidate.getUserId());
            break;
          }
        }
        //TODO team의 갯수가 1000_000를 넘어설시 문제가 됨
        int teamId = teamIdPq.poll();
        String teamName = "team" + teamId;
        redisService.makeTempTeam(teamName, team.size());
        tempTeamMap.put(teamName, team);
        sseService.notifyTempTeam(team, teamId);
      } else {
        for (MatchingRequestDto member : team) {
          redisService.rightPushMatchingQueue(member);
        }
      }
    }
  }

  public void joinTempTeam(JoinRequestDto joinRequestDto) {
    long userId = authService.getLoginUserId();
    String teamName = "team" + joinRequestDto.getTeamId();
    if (redisService.isTempTeamExist(teamName)) {
      if (joinRequestDto.isJoin()) {
        int count = redisService.getCurrentTempTeamSize(teamName) + 1;
        if (count == redisService.getTotalTempTeamSize(teamName)) {
          makeTeam(tempTeamMap.get(teamName));
          redisService.removeTempTeam(teamName);
          return;
        }
        redisService.modifyCurrentTempTeamSize(teamName);
      } else {
        List<MatchingRequestDto> team = tempTeamMap.get(teamName);
      }
    } else {

    }
  }

  public void makeTeam(List<MatchingRequestDto> team) {
    //TODO 식당 선정 로직
    RestaurantDto restaurantDto = team.getFirst().getRestaurantDto();
    //TODO 식당 저장 로직
    //TODO matchingDto에 식당추가
    MatchingDto matchingDto = MatchingDto.builder().count(team.size())
        .status(MatchingStatus.MATCHED).build();
    saveTeam(matchingDto);
    sseService.notifyTeam(team);
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
    if (member1.getGroupSize() != member2.getGroupSize()) {
      return false;
    }
    return checkDistanceCondition(member1, member2);
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
