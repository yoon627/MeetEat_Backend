package com.zb.meeteat.domain.report.controller;

import com.zb.meeteat.domain.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report")
public class ReportController {

  private final ReportService reportService;

  @PostMapping
  public void reportUser(@RequestParam long reportedId, @RequestParam long matchingId) {
    reportService.reportUser(reportedId, matchingId);
  }

  @DeleteMapping
  public void deleteReport(@RequestParam int reportedId, @RequestParam int matchingId) {
    reportService.deleteReport(reportedId, matchingId);
  }

  @GetMapping
  public ResponseEntity<Boolean> checkReport(@RequestParam int reportedId,
      @RequestParam int matchingId) {
    return ResponseEntity.ok(reportService.checkReport(reportedId, matchingId));
  }
}
