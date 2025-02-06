package com.zb.meeteat.domain.matching.service;

import com.zb.meeteat.domain.matching.dto.JoinRequestDto;
import com.zb.meeteat.domain.matching.dto.MatchingDto;
import com.zb.meeteat.domain.matching.dto.MatchingRequestDto;
import com.zb.meeteat.domain.matching.dto.TempTeamResponseDto;
import com.zb.meeteat.domain.matching.repository.MatchingRepository;
import com.zb.meeteat.domain.restaurant.dto.RestaurantDto;
import com.zb.meeteat.type.MatchingStatus;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

  private static final String MATCHING_QUEUE = "matching_queue";
  private static final String AGREE_COUNT = "agree_count";
  private static final String GROUP_SIZE = "group_size";
  private static final double EARTH_RADIUS = 6371.0;
  private final MatchingRepository matchingRepository;
  private final int MAX_SEARCHING_TIME = 100;
  private final int RESPONSE_WAIT_TIME = 180;
  private final RedisTemplate<String, MatchingRequestDto> redisTemplate;
  private final RedisTemplate<String, String> redisTeamTemplate;
  private final Map<Long, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();
  // 계산되고 있다가 취소하는 유저가 발생하는 경우를 고려하기 위한 Set
  private final Set<Long> cancelledUserSet = ConcurrentHashMap.newKeySet();
  private final PriorityQueue<Integer> teamIdPq = new PriorityQueue<>();
  private final Map<String, List<MatchingRequestDto>> tempTeamMap = new ConcurrentHashMap<>();

  @PostConstruct
  public void initTeamIdQueue() {
    for (int i = 1; i <= 1000000; i++) {
      teamIdPq.add(i);
    }
  }

  public SseEmitter subscribe() {
    //TODO 임시로 userId를 정해놓음
    long userId = 1L;
    SseEmitter sseEmitter = new SseEmitter(600_000L); // connectionTimeOut 10분
    try {
      sseEmitter.send(SseEmitter.event().name("match").data("SSE Connected"));
    } catch (IOException e) {
      sseEmitter.completeWithError(e);
    }
    sseEmitterMap.put(userId, sseEmitter);
    sseEmitter.onCompletion(
        () -> sseEmitterMap.remove(userId)); // sse 연결종료시 map에서 삭제
    sseEmitter.onTimeout(
        () -> sseEmitterMap.remove(userId)); // TODO:타임아웃이 된 경우 다시 매칭할건지 물어봐야함
    sseEmitter.onError(
        (e) -> sseEmitterMap.remove(userId)); // TODO: sseEmitter 연결에 오류가 발생할 경우 지수 백오프 방식 도입
    return sseEmitter;
  }

  // 매칭 요청
  public void requestMatching(MatchingRequestDto matchingRequestDto) {
    //TODO 10분만 매칭하는 로직추가
    //TODO 임시로 userId를 정해놓음
    long userId = 1L;
    matchingRequestDto.setUserId(userId);
    redisTemplate.opsForList().rightPush(MATCHING_QUEUE, matchingRequestDto);
  }

  public void cancelMatching() {
    //TODO 임시로 userId를 정해놓음
    long userId = 1L;
    SseEmitter emitter = sseEmitterMap.get(userId);
    emitter.complete();
    sseEmitterMap.remove(userId);
    cancelledUserSet.add(userId);
  }

  @Scheduled(fixedDelay = 1000) // 1초마다 실행
  public void makeTeam() {
    while (redisTemplate.hasKey(MATCHING_QUEUE)
        && redisTemplate.opsForList().size(MATCHING_QUEUE) > 1) {
      MatchingRequestDto user = redisTemplate.opsForList().leftPop(MATCHING_QUEUE);
      List<MatchingRequestDto> team = new LinkedList<>();
      team.add(user);
      int cnt = 0;
      while (redisTemplate.hasKey(MATCHING_QUEUE)
          && redisTemplate.opsForList().size(MATCHING_QUEUE) > 0 && cnt++ < MAX_SEARCHING_TIME
          || team.size() < user.getGroupSize()) {
        MatchingRequestDto candidate = redisTemplate.opsForList().leftPop(MATCHING_QUEUE);
        if (cancelledUserSet.contains(candidate.getUserId())) {
          cancelledUserSet.remove(candidate.getUserId());
          continue;
        }
        if (checkTeam(team, candidate)) {
          team.add(candidate);
        } else {
          redisTemplate.opsForList().rightPush(MATCHING_QUEUE, candidate);
        }
      }
      if (team.size() == user.getGroupSize()) {
        for (MatchingRequestDto candidate : team) {
          if (cancelledUserSet.contains(candidate.getUserId())) {
            for (MatchingRequestDto cancelled : team) {
              if (!cancelledUserSet.contains(cancelled.getUserId())) {
                redisTemplate.opsForList().rightPush(MATCHING_QUEUE, cancelled);
              }
            }
            cancelledUserSet.remove(candidate.getUserId());
            break;
          }
        }
        //TODO team의 갯수가 1000_000를 넘어설시 문제가 됨
        int teamId = teamIdPq.poll();
        String teamName = "team" + teamId;
        redisTeamTemplate.opsForHash()
            .put(teamName, AGREE_COUNT, "0");
        redisTeamTemplate.opsForHash().put(teamName, GROUP_SIZE, String.valueOf(team.size()));
        redisTeamTemplate.expire(teamName, RESPONSE_WAIT_TIME, TimeUnit.SECONDS);
        tempTeamMap.put(teamName, team);
        notifyTeam(team, teamId);
      } else {
        for (MatchingRequestDto member : team) {
          redisTemplate.opsForList().rightPush(MATCHING_QUEUE, member);
        }
      }
    }
  }

  //매칭 수락/거절
  public void responseMatching(JoinRequestDto joinRequestDto) {
    long userId = 1L;
    String teamName = "team" + joinRequestDto.getTeamId();
    if (redisTeamTemplate.hasKey(teamName)) {
      if (joinRequestDto.isJoin()) {
        int count = Integer.parseInt(
            (String) redisTeamTemplate.opsForHash().get(teamName, AGREE_COUNT)) + 1;
        if (count == Integer.parseInt(
            (String) redisTeamTemplate.opsForHash().get(teamName, GROUP_SIZE))) {
          completeMatching(tempTeamMap.get(teamName));
          redisTeamTemplate.delete(teamName);
          return;
        }
        redisTeamTemplate.opsForHash().put(teamName, AGREE_COUNT, String.valueOf(count));
      } else {
        List<MatchingRequestDto> team = tempTeamMap.get(teamName);
      }
    } else {

    }
  }

  //완성된 팀을 모임으로 생성
  public void completeMatching(List<MatchingRequestDto> team) {
    //TODO 식당 선정 로직
    RestaurantDto restaurantDto = team.getFirst().getRestaurantDto();
    //TODO 식당 저장 로직
    //TODO matchingDto에 식당추가
    MatchingDto matchingDto = MatchingDto.builder().count(team.size())
        .status(MatchingStatus.MATCHED).build();
    saveMatching(matchingDto);
    //TODO 사람들에게 알림
    //TODO 매칭내역 저장
  }

  public void saveMatching(MatchingDto matchingDto) {
    matchingRepository.save(MatchingDto.toEntity(matchingDto));
  }

  //임시 생성된 팀을 사람들에게 알림
  private void notifyTeam(List<MatchingRequestDto> team, int teamId) {
    TempTeamResponseDto responseDto = new TempTeamResponseDto();
    responseDto.setTeamId(teamId);
    responseDto.setMessage("임시 모임이 생성되었습니다.");
    for (MatchingRequestDto member : team) {
      responseDto.getRestaurantList().add(member.getRestaurantDto());
    }
    for (MatchingRequestDto m : team) {
      sendTeamEvent(m.getUserId(), responseDto);
    }
  }

  //임시팀 사람들에게 알림을 보냄
  private void sendTeamEvent(Long userId, TempTeamResponseDto tempTeamResponseDto) {
    SseEmitter emitter = sseEmitterMap.get(userId);
    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event().name("match").data(tempTeamResponseDto));
      } catch (IOException e) {
        emitter.complete();
        sseEmitterMap.remove(userId);
      }
    }
  }


  //팀내 사람들과 조건이 맞는지 확인
  private boolean checkTeam(List<MatchingRequestDto> team, MatchingRequestDto member) {
    for (MatchingRequestDto m : team) {
      if (!checkCondition(m, member)) {
        return false;
      }
    }
    return true;
  }

  //사람끼리 조건이 맞는지 확인
  private boolean checkCondition(MatchingRequestDto member1, MatchingRequestDto member2) {
    if (member1.getGroupSize() != member2.getGroupSize()) {
      return false;
    }
    return distanceCheck(member1, member2);
  }

  //거리가 맞는지 확인
  private boolean distanceCheck(MatchingRequestDto member1, MatchingRequestDto member2) {
    if (calculateDistance(member1.getUserLat(), member1.getUserLon(),
        member2.getRestaurantDto().getLat(), member2.getRestaurantDto().getLon()) > 2) {
      return false;
    }
    if (calculateDistance(member2.getUserLat(), member2.getUserLon(),
        member1.getRestaurantDto().getLat(), member1.getRestaurantDto().getLon()) > 2) {
      return false;
    }
    return true;
  }

  //두 지점사이 거리를 계산
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
