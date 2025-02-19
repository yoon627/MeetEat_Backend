package com.zb.meeteat.domain.ban.service;

import com.zb.meeteat.domain.ban.entity.Ban;
import com.zb.meeteat.domain.ban.repository.BanRepository;
import com.zb.meeteat.domain.user.entity.User;
import com.zb.meeteat.domain.user.repository.UserRepository;
import com.zb.meeteat.domain.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BanService {

  private final AuthService authService;
  private final BanRepository banRepository;
  private final UserRepository userRepository;

  public void banUser(long bannedId) {
    Long userId = authService.getLoggedInUserId();
    User banner = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    User banned = userRepository.findById(bannedId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    Ban ban = Ban.builder()
        .banned(banned)
        .banner(banner)
        .build();
    banRepository.save(ban);
  }

  @Transactional
  public void deleteBan(long bannedId) {
    Long userId = authService.getLoggedInUserId();
    banRepository.deleteByBannerIdAndBannedId(userId, bannedId);
  }

  public boolean checkBan(long bannedId) {
    Long userId = authService.getLoggedInUserId();
    Ban ban = banRepository.findByBannerIdAndBannedId(userId, bannedId);
    return ban != null;
  }
}
