package com.zb.meeteat.domain.report.entity;

import com.zb.meeteat.domain.matching.entity.Matching;
import com.zb.meeteat.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Report {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long reportedId;
  @ManyToOne
  @JoinColumn(name = "reporter_id")
  private User reporter;
  @ManyToOne
  @JoinColumn(name = "matching_id")
  private Matching matching;
  @CreatedDate
  private LocalDateTime createdAt;
}
