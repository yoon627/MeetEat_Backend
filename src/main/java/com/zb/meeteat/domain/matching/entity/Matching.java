package com.zb.meeteat.domain.matching.entity;

import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import com.zb.meeteat.type.MatchingStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "matching")
public class Matching {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  private int count;// 인원수

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MatchingStatus status;  // 매칭상태

  @CreatedDate
  private LocalDateTime createdAt;  // 생성시간

  @ManyToOne
  @JoinColumn(name = "restaurant_id", nullable = false)
  private Restaurant restaurant;  // 식당아이디

  @OneToMany(mappedBy = "id", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<MatchingHistory> history;
}
