package com.zb.meeteat.domain.sse.service;

import com.zb.meeteat.domain.matching.dto.JoinRequestDto;
import com.zb.meeteat.domain.matching.dto.JoinResponseDto;
import com.zb.meeteat.domain.matching.dto.MatchingDto;
import com.zb.meeteat.domain.matching.dto.MatchingRequestDto;
import com.zb.meeteat.domain.matching.dto.TeamResponseDto;
import com.zb.meeteat.domain.matching.dto.TempTeamResponseDto;
import com.zb.meeteat.domain.matching.dto.UserJoinDto;
import com.zb.meeteat.domain.matching.dto.UserMatchingHistoryDto;
import com.zb.meeteat.domain.matching.entity.Matching;
import com.zb.meeteat.domain.matching.entity.MatchingHistory;
import com.zb.meeteat.domain.matching.repository.MatchingHistoryRepository;
import com.zb.meeteat.domain.restaurant.dto.RestaurantDto;
import com.zb.meeteat.domain.user.entity.User;
import com.zb.meeteat.domain.user.repository.UserRepository;
import com.zb.meeteat.domain.user.service.AuthService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  private final UserRepository userRepository;
  private final Set<Long> cancelledUserSet = ConcurrentHashMap.newKeySet();//취소한 유저가 계산되고 있는 경우를 체크하기 위해
  private final MatchingHistoryRepository matchingHistoryRepository;


  public SseEmitter subscribe() {
    long userId = authService.getLoggedInUserId();
    SseEmitter sseEmitter = new SseEmitter(4_200_000L); // connectionTimeOut 70분
    try {
      sseEmitter.send(SseEmitter.event().name("match").data("SSE Connected"));
    } catch (IOException e) {
      sseEmitter.completeWithError(e);
    }
    sseEmitterMap.put(userId, sseEmitter);
    sseEmitter.onCompletion(
        () -> sseEmitterMap.remove(userId)); // sse 연결종료시 map에서 삭제
    sseEmitter.onTimeout(
        () -> {
          sendConnectionTimeOutEvent(userId);
          cancelledUserSet.add(userId);
          sseEmitterMap.remove(userId);
        }); // TODO:타임아웃이 된 경우 다시 매칭할건지 물어봐야함
    sseEmitter.onError(
        (e) -> {
          cancelledUserSet.add(userId);
          sseEmitterMap.remove(userId);
        }); // TODO: sseEmitter 연결에 오류가 발생할 경우 지수 백오프 방식 도입
    return sseEmitter;
  }

  public void unsubscribe(Long userId) {
    sseEmitterMap.remove(userId);
  }


  public void notifyTempTeam(List<MatchingRequestDto> team, int teamId) {
    List<RestaurantDto> restaurantDtos = new ArrayList<>();
    for (MatchingRequestDto member : team) {
      RestaurantDto restaurantDto = member.getPlace();
      User user = userRepository.findById(member.getUserId()).orElseThrow(RuntimeException::new);
      restaurantDto.setUser(
          UserMatchingHistoryDto.builder().id(member.getUserId()).nickname(user.getNickname())
              .build());
      restaurantDto.setPlace(RestaurantDto.toPlaceDto(restaurantDto));
      restaurantDtos.add(restaurantDto);
    }
    TempTeamResponseDto responseDto = TempTeamResponseDto.builder().teamId(teamId)
        .message("임시 모임이 생성되었습니다.").restaurantList(restaurantDtos).build();

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
      log.info("join알림 당사자에게 보내지 않음");
      return;
    }
    SseEmitter emitter = sseEmitterMap.get(userId);
    log.info("tempTeamJoin emitter: {}", emitter);
    if (emitter != null) {
      log.info("tempTeamJoin emitter: null 아님 {}", emitter);
      try {
        User user = userRepository.findById(joinRequestDto.getUserId())
            .orElseThrow(RuntimeException::new);
        UserJoinDto userJoinDto = UserJoinDto.builder()
            .id(userId)
            .nickname(user.getNickname())
            .join(joinRequestDto.isJoin())
            .build();
        JoinResponseDto joinResponseDto = JoinResponseDto.builder()
            .message("수락 알림이 도착했습니다.")
            .user(userJoinDto)
            .build();
        emitter.send(SseEmitter.event().name("Join").data(joinResponseDto));
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

  public void notifyTeam(RestaurantDto restaurantDto, List<MatchingRequestDto> team,
      Matching matching) {
    List<UserMatchingHistoryDto> userList = new ArrayList<>();
    for (MatchingRequestDto m : team) {
      User user = userRepository.findById(m.getUserId()).orElseThrow(RuntimeException::new);
      userList.add(UserMatchingHistoryDto.builder().id(m.getUserId()).nickname(user.getNickname())
          .introduce(user.getIntroduce()).join(true).build());
    }
    TeamResponseDto teamResponseDto = TeamResponseDto.builder().message("팀 생성이 완료되었습니다.")
        .matching(
            MatchingDto.builder().id(matching.getId()).userList(userList).restaurant(restaurantDto)
                .createdAt(matching.getCreatedAt())
                .build())
        .build();
    log.info("생성된 Team Id: {}", teamResponseDto.getMatching().getId());
    log.info("생성된 Team Id: {}", teamResponseDto.getMatching().getId());
    log.info("생성된 Team Id: {}", teamResponseDto.getMatching().getId());
    log.info("생성된 Team Id: {}", teamResponseDto.getMatching().getId());
    log.info("생성된 Team Id: {}", teamResponseDto.getMatching().getId());
    log.info("생성된 Team Id: {}", teamResponseDto.getMatching().getId());
    log.info("생성된 Team Id: {}", teamResponseDto.getMatching().getId());
    log.info("생성된 Team Id: {}", teamResponseDto.getMatching().getId());
    for (MatchingRequestDto matchingRequestDto : team) {
      sendTeamEvent(matchingRequestDto.getUserId(), teamResponseDto);
    }
  }

  public void sendTeamEvent(long userId, TeamResponseDto teamResponseDto) {
    SseEmitter emitter = sseEmitterMap.get(userId);
    log.info("team emitter: {}", emitter);
    if (emitter != null) {
      log.info("emitter: null아님 {}", emitter);
      MatchingHistory matchingHistory = matchingHistoryRepository.findByMatchingIdAndUserId(
          teamResponseDto.getMatching().getId(), userId);
      teamResponseDto.setMatchingHistoryId(matchingHistory.getId());
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

  public void sendConnectionTimeOutEvent(long userId) {
    SseEmitter emitter = sseEmitterMap.get(userId);
    log.info("team emitter: {}", emitter);
    if (emitter != null) {
      log.info("emitter: null아님 {}", emitter);
      try {
        log.info("연결시간 초과 알림 보내기 중: {}", emitter);
        emitter.send(SseEmitter.event().name("TimeOut").data("timeout"));
        log.info("연결시간 초과 알림 보내기 완료:{}", emitter);
      } catch (IOException e) {
        emitter.complete();
        sseEmitterMap.remove(userId);
        log.info("Team emitter 연결 끊기: {}", emitter);
      }
    } else {
      log.info("emitter: null임 {}", emitter);

    }
  }

  public void notifyCancelledMatching(Matching matching, List<UserMatchingHistoryDto> userList,
      Long cancelledUserId) {
    for (UserMatchingHistoryDto userMatchingHistoryDto : userList) {
      long userId = userMatchingHistoryDto.getId();
      if (userId == cancelledUserId) {
        continue;
      }
      SseEmitter emitter = sseEmitterMap.get(userId);
      log.info("team emitter: {}", emitter);
      if (emitter != null) {
        log.info("emitter: null아님 {}", emitter);
        try {
          log.info("매칭 취소 알림 보내기 중: {}", emitter);
          emitter.send(SseEmitter.event().name("cancel").data(cancelledUserId));
          log.info("매칭 취소 알림 보내기 완료:{}", emitter);
          sseEmitterMap.remove(userId);
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

  public void notifyEscapedMatching(Matching matching, List<UserMatchingHistoryDto> userList,
      Long cancelledUserId) {
    for (UserMatchingHistoryDto userMatchingHistoryDto : userList) {
      long userId = userMatchingHistoryDto.getId();
      if (userId == cancelledUserId) {
        continue;
      }
      SseEmitter emitter = sseEmitterMap.get(userId);
      log.info("team emitter: {}", emitter);
      if (emitter != null) {
        log.info("emitter: null아님 {}", emitter);
        try {
          log.info("매칭 취소 알림 보내기 중: {}", emitter);
          emitter.send(SseEmitter.event().name("escape").data(cancelledUserId));
          log.info("매칭 취소 알림 보내기 완료:{}", emitter);
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

  public void addCancelledUserSet(long userId) {
    cancelledUserSet.add(userId);
  }

  public void removeCancelledUserSet(long userId) {
    cancelledUserSet.remove(userId);
  }

  public boolean checkCancelledUserSet(long userId) {
    if (!sseEmitterMap.containsKey(userId)) {
      return true;
    }
    if (sseEmitterMap.get(userId) == null) {
      sseEmitterMap.remove(userId);
      return true;
    }
    return cancelledUserSet.contains(userId);
  }

  public boolean checkConnection(long userId) {
    return sseEmitterMap.containsKey(userId) && sseEmitterMap.get(userId) != null;
  }

  public boolean selfCheckConnection() {
    long userId = authService.getLoggedInUserId();
    return sseEmitterMap.containsKey(userId) && sseEmitterMap.get(userId) != null;
  }

}
