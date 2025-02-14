package com.zb.meeteat.domain.matching.repository;

import com.zb.meeteat.domain.matching.entity.MatchingHistory;
import com.zb.meeteat.domain.matching.entity.MatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingHistoryRepository extends JpaRepository<MatchingHistory, Long> {

  boolean existsByUserIdAndStatus(Long userId, MatchingStatus status);
}
