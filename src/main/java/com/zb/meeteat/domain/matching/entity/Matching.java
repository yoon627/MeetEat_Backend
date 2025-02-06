package com.zb.meeteat.domain.matching.entity;

import com.zb.meeteat.type.MatchingStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
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
public class Matching {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private int count;
  private MatchingStatus status;
  private long restaurantId;
  @CreatedDate
  private LocalDateTime createdAt;
//  private long restaurantId;
}
