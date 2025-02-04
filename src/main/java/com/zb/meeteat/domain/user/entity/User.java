package com.zb.meeteat.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "nickname")
})
public class User extends BaseEntity {

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
    private SignUpType signupType; // 가입 유형 (EMAIL / KAKAO / NAVER)

    private Integer matchingCount = 0; // 매칭 횟수 (기본값 0)

    private Boolean isPenalty = false; // 패널티 여부 (기본값 false)

    private LocalDateTime bannedAt; // 계정 정지 날짜

    private LocalDateTime createAt; // 계정 생성일
    private LocalDateTime updateAt; // 계정 수정일

    @Builder
    public User(String email, String password, String nickname, String introduce,
                Role role, SignUpType signupType, Integer matchingCount,
                Boolean isPenalty, LocalDateTime bannedAt) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.introduce = introduce;
        this.role = role;
        this.signupType = signupType;
        this.matchingCount = matchingCount;
        this.isPenalty = isPenalty;
        this.bannedAt = bannedAt;
    }

    // 비밀번호 업데이트 메서드
    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.updateAt = LocalDateTime.now();
    }

    // 닉네임 변경 메서드
    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
        this.updateAt = LocalDateTime.now();
    }



}
