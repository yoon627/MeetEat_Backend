package com.zb.meeteat.domain.user.service;

import com.zb.meeteat.domain.matching.repository.MatchingHistoryRepository;
import com.zb.meeteat.domain.user.dto.UserProfileResponse;
import com.zb.meeteat.domain.user.entity.User;
import com.zb.meeteat.domain.user.repository.UserRepository;
import com.zb.meeteat.exception.CustomException;
import com.zb.meeteat.exception.ErrorCode;
import com.zb.meeteat.type.MatchingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final MatchingHistoryRepository matchingHistoryRepository;
  private final RedisTemplate<String, String> redisTemplate;

  @Transactional
  public UserProfileResponse getUserProfile(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    return new UserProfileResponse(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getIntroduce(),
        user.getRole().name(),
        user.getSignupType().name(),
        user.getMatchingCount(),
        user.getIsPenalty(),
        user.getBannedAt(),
        user.getCreatedAt(),
        user.getUpdatedAt()
    );
  }

  @Transactional
  public void updateNickname(User user, String newNickname) {
    // 닉네임 중복 검사
    if (userRepository.existsByNickname(newNickname)) {
      throw new CustomException(ErrorCode.NICKNAME_ALREADY_REGISTERED);
    }

    // 닉네임 변경
    user.updateNickname(newNickname);
    userRepository.save(user);
  }

  @Transactional
  public void updateIntroduce(User user, String newIntroduce) {
    user.updateIntroduce(newIntroduce);
    userRepository.save(user);
  }

  @Transactional
  public void withdrawUser(User user) {
    // 현재 진행 중인 매칭 확인
    boolean hasOngoingMatching = matchingHistoryRepository.existsByUserIdAndStatus(user.getId(),
        MatchingStatus.MATCHED);

    if (hasOngoingMatching) {
      throw new CustomException(ErrorCode.USER_HAS_ONGOING_MATCHING);
    }

    // Redis에서 firstLogin 데이터 삭제
    String redisKey = "firstLogin:" + user.getId();
    redisTemplate.delete(redisKey);
    log.info("Redis에서 firstLogin 데이터 삭제 완료: {}", redisKey);

    // 유저 상태를 탈퇴 처리
    userRepository.delete(user);
    log.info("회원 탈퇴 처리 완료: {}", user.getEmail());
  }


}

