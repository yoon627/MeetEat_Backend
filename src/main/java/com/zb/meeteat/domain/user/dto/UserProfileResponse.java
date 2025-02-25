package com.zb.meeteat.domain.user.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileResponse {

  private Long id;
  private String email;
  private String nickname;
  private String introduce;
  private String role;
  private String signupType;
  private int matchingCount;
  private Boolean isPenalty;
  private LocalDateTime bannedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
