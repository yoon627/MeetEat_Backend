package com.zb.meeteat.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@Entity
@Table(name = "user")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, unique = false)
  private String nickname;

  private String introduce;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role; // 사용자 권한 (ADMIN / USER)

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SignupType signupType; // 가입 유형 (EMAIL / KAKAO / NAVER)

  private Integer matchingCount = 0; // 매칭 횟수 (기본값 0)

  private Boolean isPenalty = false; // 패널티 여부 (기본값 false)

  private LocalDateTime bannedAt; // 계정 정지 날짜

  private LocalDateTime createAt; // 계정 생성일
  private LocalDateTime updateAt; // 계정 수정일

}