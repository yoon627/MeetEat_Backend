package com.zb.meeteat.domain.matching.controller;

import com.zb.meeteat.domain.matching.dto.MatchingHistoryDto;
import com.zb.meeteat.domain.matching.service.MatchingHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matching")
public class MatchingHistoryController {

  private final MatchingHistoryService matchingHistoryService;

  @GetMapping("/history")
  public ResponseEntity<Page<MatchingHistoryDto>> getMatchingHistory(
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "4") int size) {
    var result = matchingHistoryService.getMatchingHistory(page, size);
    return ResponseEntity.ok(result);
  }

  @GetMapping
  public ResponseEntity<?> getRecentMatching() {
    var result = matchingHistoryService.getRecentMatching();
    return ResponseEntity.ok(result == null ? "null" : result);
  }

  @PostMapping("/{matchingId}")
  public ResponseEntity<?> cancelMatchingHistory(@PathVariable long matchingId) {
    matchingHistoryService.cancelMatchingHistory(matchingId);
    return ResponseEntity.ok(null);
  }

  @PostMapping("/history/{matchingHistoryId}")
  public void reviewLaterMatchingHistory(@PathVariable long matchingHistoryId) {
    matchingHistoryService.reviewLaterMatchingHistory(matchingHistoryId);
  }

}
