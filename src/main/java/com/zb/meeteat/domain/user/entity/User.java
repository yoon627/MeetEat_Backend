package com.zb.meeteat.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "users", uniqueConstraints = {
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

}

