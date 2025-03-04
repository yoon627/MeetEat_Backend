package com.zb.meeteat.domain.report.repository;

import com.zb.meeteat.domain.report.entity.Report;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

  void deleteByReporterIdAndReportedIdAndMatchingId(Long reporter, Long reportedId,
      Long matchingId);

  Report findByReporterIdAndReportedIdAndMatchingId(Long userId, Long reportedId, Long matchingId);

  List<Report> findByReportedIdAndMatchingId(long reportedId, long matchingId);
}
