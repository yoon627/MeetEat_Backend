package com.zb.meeteat.domain.report.repository;

import com.zb.meeteat.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

  void deleteByReportedIdAndReportedIdAndMatchingId(Long userId, int reportedId, int matchingId);

  Report findByReporterIdAndReportedIdAndMatchingId(Long userId, int reportedId, int matchingId);
}
