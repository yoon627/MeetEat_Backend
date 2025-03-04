package com.zb.meeteat.domain.report.service;

import com.zb.meeteat.domain.matching.entity.Matching;
import com.zb.meeteat.domain.matching.repository.MatchingRepository;
import com.zb.meeteat.domain.report.entity.Report;
import com.zb.meeteat.domain.report.repository.ReportRepository;
import com.zb.meeteat.domain.user.entity.User;
import com.zb.meeteat.domain.user.repository.UserRepository;
import com.zb.meeteat.domain.user.service.AuthService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

  private final AuthService authService;
  private final ReportRepository reportRepository;
  private final MatchingRepository matchingRepository;
  private final UserRepository userRepository;

  public void reportUser(long reportedId, long matchingId) {
    Long userId = authService.getLoggedInUserId();
    Matching matching = matchingRepository.findById(matchingId)
        .orElseThrow(() -> new RuntimeException("Matching not found"));
    User reporter = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    Report report = Report.builder()
        .reportedId(reportedId)
        .reporter(reporter)
        .matching(matching)
        .build();
    reportRepository.save(report);
    List<Report> reports = reportRepository.findByReportedIdAndMatchingId(reportedId, matchingId);
    if (matching.getCount() > 2 && reports.size() > matching.getCount() / 2) {
      User reportedUser = userRepository.findById(reportedId)
          .orElseThrow(() -> new RuntimeException("User not found"));
      reportedUser.setIsPenalty(true);
      reportedUser.setBannedAt(LocalDateTime.now());
      reportedUser.setBannedEndAt(LocalDateTime.now().plusDays(7L));
      userRepository.save(reportedUser);
    }
  }

  @Transactional
  public void deleteReport(long reportedId, long matchingId) {
    Long userId = authService.getLoggedInUserId();
    reportRepository.deleteByReporterIdAndReportedIdAndMatchingId(userId, reportedId, matchingId);
  }

  public boolean checkReport(long reportedId, long matchingId) {
    Long userId = authService.getLoggedInUserId();
    Report report = reportRepository.findByReporterIdAndReportedIdAndMatchingId(userId, reportedId,
        matchingId);
    return report != null;
  }

  public boolean checkReport(long reporterId, long reportedId, long matchingId) {
    Report report = reportRepository.findByReporterIdAndReportedIdAndMatchingId(reporterId,
        reportedId,
        matchingId);
    return report != null;
  }
}
