package com.zb.meeteat.domain.matching.entity;

import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@Entity
@Table(name = "matching")
public class Matching {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;  // 매칭 모임 아이디
  private int count;// 인원수

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MatchingStatus status;  // 매칭상태

  private LocalDateTime createdAt;  // 생성시간

  @ManyToOne
  @JoinColumn(name = "restaurant_id", nullable = false)
  private Restaurant restaurant;  // 식당아이디

  @OneToMany(mappedBy = "id", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<MatchingHistory> history;
}
