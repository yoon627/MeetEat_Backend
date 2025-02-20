package com.zb.meeteat.domain.user.entity;

import com.zb.meeteat.external.kakao.KakaoUserResponse;
import com.zb.meeteat.external.naver.NaverUserResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "`user`", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = "nickname")
})
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

  @Column(length = 50)
  private String introduce;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role; // 사용자 권한 (ADMIN / USER)

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SignUpType signupType; // 가입 유형 (EMAIL / KAKAO / NAVER)

  @Builder.Default
  private Integer matchingCount = 0; // 매칭 횟수 (기본값 0)

  @Builder.Default
  private Boolean isPenalty = false; // 패널티 여부 (기본값 false)

  private LocalDateTime bannedAt; // 계정 정지 날짜

  private LocalDateTime bannedEndAt; // 계정 정지 해제 날짜

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public boolean isProfileIncomplete() {
    return this.nickname == null || this.nickname.isEmpty()
        || this.introduce == null || this.introduce.isEmpty();
  }


  public static User ofKakao(KakaoUserResponse response) {
    String email = response.getKakaoAccount().getEmail();
    String nickname = response.getKakaoAccount().getNickname();
    String introduce = "안녕하세요! MeetEat을 이용해주셔서 감사합니다.";

    if (email == null || email.isEmpty()) {
      email = "kakao_" + UUID.randomUUID().toString().substring(0, 8) + "@temp.com";
    }

    if (nickname == null || nickname.isEmpty()) {
      nickname = UUID.randomUUID().toString().substring(0, 6);
    }

    log.info("카카오 사용자 생성 - 이메일: {}, 닉네임: {}, 소개: {}", email, nickname, introduce);

    return User.builder()
        .email(email)
        .nickname(nickname)
        .introduce(introduce)  // 기본 소개 추가
        .password(UUID.randomUUID().toString())  // 더미 비밀번호 설정
        .signupType(SignUpType.KAKAO)
        .role(Role.USER)
        .build();
  }


  public static User ofNaver(NaverUserResponse response) {
    String email = response.getResponse().getEmail();
    String nickname = response.getResponse().getNickname();

    if (email == null || email.isEmpty()) {
      email = "naver_" + UUID.randomUUID().toString().substring(0, 8) + "@temp.com";
    }

    if (nickname == null || nickname.isEmpty()) {
      nickname = UUID.randomUUID().toString().substring(0, 6);
    }

    return User.builder()
        .email(email)
        .nickname(nickname)
        .password(UUID.randomUUID().toString())
        .signupType(SignUpType.NAVER)
        .role(Role.USER)
        .build();
  }

  public void updateNickname(String newNickname) {
    this.nickname = newNickname;
    this.updatedAt = LocalDateTime.now();
  }

  public void updateIntroduce(String newIntroduce) {
    this.introduce = newIntroduce;
    this.updatedAt = LocalDateTime.now();
  }

  public void updateFromKakao(KakaoUserResponse response) {
    String newNickname = response.getKakaoAccount().getNickname();
    if (newNickname != null && !newNickname.isEmpty()) {
      this.nickname = newNickname;
    }
  }

  public void updateFromNaver(NaverUserResponse response) {
    String newNickname = response.getResponse().getNickname();
    if (newNickname != null && !newNickname.isEmpty()) {
      this.nickname = newNickname;
    }
  }



}

