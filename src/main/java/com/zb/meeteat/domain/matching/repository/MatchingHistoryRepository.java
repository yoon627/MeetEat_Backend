package com.zb.meeteat.domain.matching.repository;

import com.zb.meeteat.domain.matching.entity.MatchingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingHistoryRepository extends JpaRepository<MatchingHistory, Long> {

}
