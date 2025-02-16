package com.zb.meeteat.domain.sse.service;

import com.zb.meeteat.domain.matching.dto.JoinRequestDto;
import com.zb.meeteat.domain.matching.dto.MatchingDto;
import com.zb.meeteat.domain.matching.dto.MatchingRequestDto;
import com.zb.meeteat.domain.matching.dto.TeamResponseDto;
import com.zb.meeteat.domain.matching.dto.TempTeamResponseDto;
import com.zb.meeteat.domain.restaurant.dto.RestaurantDto;
import com.zb.meeteat.domain.user.service.AuthService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

  private final AuthService authService;
  private final Map<Long, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

  public SseEmitter subscribe() {
    long userId = authService.getLoggedInUserId();
    SseEmitter sseEmitter = new SseEmitter(600_0000L); // connectionTimeOut 10분
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

  public void unsubscribe(Long userId) {
    SseEmitter sseEmitter = sseEmitterMap.get(userId);
    sseEmitter.complete();
    sseEmitterMap.remove(userId);
  }


  public void notifyTempTeam(List<MatchingRequestDto> team, int teamId) {
    TempTeamResponseDto responseDto = new TempTeamResponseDto();
    responseDto.setTeamId(teamId);
    responseDto.setMessage("임시 모임이 생성되었습니다.");
    for (MatchingRequestDto member : team) {
      responseDto.getRestaurantList().add(member.getRestaurantDto());
    }
    for (MatchingRequestDto m : team) {
      sendTempTeamEvent(m.getUserId(), responseDto);
    }
  }

  public void notifyTempTeamJoin(List<MatchingRequestDto> team, JoinRequestDto joinRequestDto) {
    for (MatchingRequestDto m : team) {
      sendTempTeamJoinEvent(m.getUserId(), joinRequestDto);
    }
  }

  public void sendTempTeamJoinEvent(Long userId, JoinRequestDto joinRequestDto) {
    if (userId == joinRequestDto.getUserId()) {
      return;
    }
    SseEmitter emitter = sseEmitterMap.get(userId);
    log.info("tempTeamJoin emitter: {}", emitter);
    if (emitter != null) {
      log.info("tempTeamJoin emitter: null 아님 {}", emitter);
      try {
        emitter.send(SseEmitter.event().name("Join").data(joinRequestDto));
        log.info("tempTeamJoin 알림 보냄: {}", emitter);
      } catch (IOException e) {
        emitter.complete();
        sseEmitterMap.remove(userId);
        log.info("tempTeamJoin emitter 연결 끊기: {}", emitter);
      }
    } else {
      log.info("tempTeamJoin emitter: null {}", emitter);
    }
  }

  public void sendTempTeamEvent(Long userId, TempTeamResponseDto tempTeamResponseDto) {
    SseEmitter emitter = sseEmitterMap.get(userId);
    log.info("tempTeam emitter: {}", emitter);
    if (emitter != null) {
      log.info("tempTeam emitter: null 아님 {}", emitter);
      try {
        emitter.send(SseEmitter.event().name("TempTeam").data(tempTeamResponseDto));
        log.info("tempTeam 알림 보냄: {}", emitter);
      } catch (IOException e) {
        emitter.complete();
        sseEmitterMap.remove(userId);
        log.info("tempTeam emitter 연결 끊기: {}", emitter);
      }
    } else {
      log.info("tempTeam emitter: null {}", emitter);
    }
  }

  public void notifyTeam(RestaurantDto restaurantDto, List<MatchingRequestDto> team) {
    TeamResponseDto teamResponseDto = TeamResponseDto.builder().message("팀 생성이 완료되었습니다.")
        .matchingDto(MatchingDto.builder().restaurant(restaurantDto).build()).build();
    for (MatchingRequestDto matchingRequestDto : team) {
      sendTeamEvent(matchingRequestDto.getUserId(), teamResponseDto);
    }
  }

  public void sendTeamEvent(long userId, TeamResponseDto teamResponseDto) {
    SseEmitter emitter = sseEmitterMap.get(userId);
    log.info("team emitter: {}", emitter);
    if (emitter != null) {
      log.info("emitter: null아님 {}", emitter);

      try {
        log.info("팀 생성 완료 알림 보내기 중: {}", emitter);
        emitter.send(SseEmitter.event().name("Team").data(teamResponseDto));
        log.info("팀 생성 완료 알림 보내기 완료:{}", emitter);
      } catch (IOException e) {
        emitter.complete();
        sseEmitterMap.remove(userId);
        log.info("Team emitter 연결 끊기: {}", emitter);
      }
    } else {
      log.info("emitter: null임 {}", emitter);

    }
  }
}
