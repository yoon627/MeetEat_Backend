package com.zb.meeteat.domain.matching.controller;

import com.zb.meeteat.domain.matching.dto.JoinRequestDto;
import com.zb.meeteat.domain.matching.dto.MatchingRequestDto;
import com.zb.meeteat.domain.matching.dto.MatchingResponseDto;
import com.zb.meeteat.domain.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matching")
public class MatchingController {

  private final MatchingService matchingService;

  @PostMapping
  public void joinTempTeam(@RequestBody JoinRequestDto joinRequestDto) {
    matchingService.joinTempTeam(joinRequestDto);
  }

  @PostMapping("/request")
  public MatchingResponseDto requestMatching(@RequestBody MatchingRequestDto matchingRequestDto) {
    matchingService.requestMatching(matchingRequestDto);
    return MatchingResponseDto.builder().message("Matching Started").build();
  }

  @PostMapping("/cancel")
  public MatchingResponseDto cancelMatching() {
    matchingService.cancelMatching();
    return MatchingResponseDto.builder().message("Matching Cancelled").build();
  }
}