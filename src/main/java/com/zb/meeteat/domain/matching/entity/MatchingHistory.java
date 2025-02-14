package com.zb.meeteat.domain.matching.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "matchinghistory")
public class MatchingHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;  // 매칭 내역 아이디

  @Enumerated(EnumType.STRING)
  private MatchingStatus status;  // 매칭 상태
  private LocalDateTime createdAt;  // 생성일자

  @JoinColumn(name = "userId", nullable = false)
  private Long userId; // 유저 아이디

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "matchingId", nullable = false)
  private Matching matching; // 매칭 모임 아이디
}
