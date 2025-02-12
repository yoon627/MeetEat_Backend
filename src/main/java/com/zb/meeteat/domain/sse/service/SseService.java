package com.zb.meeteat.domain.sse.service;

import com.zb.meeteat.domain.matching.dto.MatchingRequestDto;
import com.zb.meeteat.domain.matching.dto.TeamResponseDto;
import com.zb.meeteat.domain.matching.dto.TempTeamResponseDto;
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

  private final Map<Long, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

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

  public void unsubscribe(Long userId) {
    SseEmitter sseEmitter = sseEmitterMap.get(userId);
    sseEmitter.complete();
    sseEmitterMap.remove(userId);
  }


  public void notifyTeam(List<MatchingRequestDto> team) {
    TeamResponseDto teamResponseDto = new TeamResponseDto();
    for (MatchingRequestDto matchingRequestDto : team) {
      sendTeamEvent(matchingRequestDto.getUserId(), teamResponseDto);
    }
  }

  public void sendTeamEvent(long userId, TeamResponseDto teamResponseDto) {
    SseEmitter emitter = sseEmitterMap.get(userId);
    if (emitter == null) {
      try {
        emitter.send(SseEmitter.event().name("match").data(teamResponseDto));
        emitter.complete();
        sseEmitterMap.remove(userId);
      } catch (IOException e) {
        emitter.complete();
        sseEmitterMap.remove(userId);
      }
    }
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

  public void sendTempTeamEvent(Long userId, TempTeamResponseDto tempTeamResponseDto) {
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
}
