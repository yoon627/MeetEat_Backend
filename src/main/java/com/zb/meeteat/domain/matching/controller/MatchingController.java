package com.zb.meeteat.domain.matching.controller;

import com.zb.meeteat.domain.matching.dto.JoinRequestDto;
import com.zb.meeteat.domain.matching.dto.MatchingRequestDto;
import com.zb.meeteat.domain.matching.dto.MatchingResponseDto;
import com.zb.meeteat.domain.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matching")
public class MatchingController {

  private final MatchingService matchingService;

  @PostMapping("/join")
  public void joinTempTeam(@RequestBody JoinRequestDto joinRequestDto) {
    log.info("join 신청이 컨트롤러에 옴");
    matchingService.joinTempTeam(joinRequestDto);
  }

  @PostMapping("/request")
  public MatchingResponseDto requestMatching(@RequestBody MatchingRequestDto matchingRequestDto) {
    matchingService.requestMatching(matchingRequestDto);
    return MatchingResponseDto.builder().message("Matching Started")
        .restaurantDto(matchingRequestDto.getPlace()).build();
  }

  @PostMapping("/cancel")
  public MatchingResponseDto cancelMatching() {
    matchingService.cancelMatching();
    return MatchingResponseDto.builder().message("Matching Cancelled").build();
  }

}