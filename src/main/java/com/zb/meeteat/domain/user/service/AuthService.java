package com.zb.meeteat.domain.user.service;

import com.zb.meeteat.domain.user.dto.AuthCodeResponseDto;
import com.zb.meeteat.domain.user.dto.ChangePasswordRequest;
import com.zb.meeteat.domain.user.dto.SigninRequestDto;
import com.zb.meeteat.domain.user.dto.SignupRequestDto;
import com.zb.meeteat.domain.user.entity.Role;
import com.zb.meeteat.domain.user.entity.SignUpType;
import com.zb.meeteat.domain.user.entity.User;
import com.zb.meeteat.domain.user.repository.UserRepository;
import com.zb.meeteat.exception.CustomException;
import com.zb.meeteat.exception.ErrorCode;
import com.zb.meeteat.jwt.JwtUtil;
import com.zb.meeteat.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final RedisTemplate<String, String> redisTemplate;

  @Transactional
  public User signup(SignupRequestDto requestDto) {
    validateDuplicateUser(requestDto.getEmail(), requestDto.getNickname());

    String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

    User user = User.builder()
        .email(requestDto.getEmail())
        .password(encodedPassword)
        .nickname(requestDto.getNickname())
        .role(Role.USER)
        .signupType(SignUpType.EMAIL)
        .build();

    return userRepository.save(user);
  }

  private void validateDuplicateUser(String email, String nickname) {
    if (userRepository.existsByEmail(email)) {
      throw new CustomException(ErrorCode.EMAIL_ALREADY_REGISTERED);
    }
    if (userRepository.existsByNickname(nickname)) {
      throw new CustomException(ErrorCode.NICKNAME_ALREADY_REGISTERED);
    }
  }

  @Transactional
  public AuthCodeResponseDto signin(SigninRequestDto request) {
    // 1. 이메일로 사용자 찾기
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // 2. 탈퇴 예정 계정인지 확인 (`isPenalty`가 true면 탈퇴 예정 상태)
    if (user.getIsPenalty()) {
      throw new CustomException(ErrorCode.USER_SCHEDULED_FOR_DELETION);
    }

    // 3. 비밀번호 검증
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
    }

    // 4. JWT 토큰 생성
    String accessToken = jwtUtil.generateToken(user);

    // 5. 프로필 업데이트 필요 여부 확인 (닉네임이 없거나 비어있는 경우 true)
    String key = "firstLogin:" + user.getId();
    Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, "true");

    // 6. 첫 로그인 여부에 따라 응답 객체 다르게 반환
    if (isNew != null && isNew) {
      return new AuthCodeResponseDto(accessToken, true);
    } else {
      return new AuthCodeResponseDto(accessToken);
    }
  }


  @Transactional
  public void signout(String token) {
    String jwt = token.replace("Bearer ", "");

    if (!jwtUtil.validateToken(jwt)) {
      log.error("로그인 직후 발급된 토큰이 유효하지 않음: {}", token);
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }

    jwtUtil.blacklistToken(jwt);
    log.info("블랙리스트 등록된 토큰: {}", jwt);

    // Redis에서 블랙리스트 확인
    boolean isBlacklisted = jwtUtil.isBlacklisted(jwt);
    log.info("로그아웃 후 블랙리스트 여부: {}", isBlacklisted);
  }

  @Transactional
  public void changePassword(User user, ChangePasswordRequest request) {
    // 1. 현재 비밀번호 검증
    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
    }

    // 2. 새 비밀번호가 현재 비밀번호와 동일한지 체크
    if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
      throw new CustomException(ErrorCode.SAME_PASSWORD);
    }

    // 3. 비밀번호 변경 후 저장
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }

  public Long getLoggedInUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    return userDetails.getId();
  }

}

