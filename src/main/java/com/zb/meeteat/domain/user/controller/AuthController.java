package com.zb.meeteat.domain.user.controller;

import com.zb.meeteat.domain.user.dto.AuthCodeResponseDto;
import com.zb.meeteat.domain.user.dto.ChangePasswordRequest;
import com.zb.meeteat.domain.user.dto.SigninRequestDto;
import com.zb.meeteat.domain.user.dto.SignupRequestDto;
import com.zb.meeteat.domain.user.dto.UpdateIntroduceRequest;
import com.zb.meeteat.domain.user.dto.UpdateNicknameRequest;
import com.zb.meeteat.domain.user.dto.UserProfileResponse;
import com.zb.meeteat.domain.user.service.AuthService;
import com.zb.meeteat.domain.user.service.SocialAuthService;
import com.zb.meeteat.domain.user.service.UserService;
import com.zb.meeteat.security.UserDetailsImpl;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final SocialAuthService socialAuthService;
  private final UserService userService;

  // 회원가입
  @PostMapping("/signup")
  public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto requestDto) {
    authService.signup(requestDto);
    return ResponseEntity.ok().build();
  }

  // 이메일 로그인
  @PostMapping("/signin")
  public ResponseEntity<AuthCodeResponseDto> signin(
      @Valid @RequestBody SigninRequestDto requestDto) {
    return ResponseEntity.ok(authService.signin(requestDto));
  }

  // 소셜 로그인
  @PostMapping("/signin/{provider}")
  public ResponseEntity<AuthCodeResponseDto> socialSignin(
      @PathVariable String provider,
      @RequestBody Map<String, String> requestBody) {

    String authCode = requestBody.get("code");

    log.info("받은 authCode: {}", authCode);

    if (authCode == null || authCode.isEmpty()) {
      throw new IllegalArgumentException("인가 코드가 제공되지 않았습니다.");
    }

    AuthCodeResponseDto response = socialAuthService.socialSignin(provider, authCode);
    return ResponseEntity.ok(response);
  }

  // 로그 아웃
  @PostMapping("/signout")
  public ResponseEntity<Void> signout(@RequestHeader("Authorization") String token) {
    authService.signout(token);
    return ResponseEntity.ok().build();
  }

  // 회원 탈퇴
  @DeleteMapping("/withdrawal")
  public ResponseEntity<String> withdrawUser(
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    log.info("회원 탈퇴 요청: {}", userDetails.getUsername());

    userService.withdrawUser(userDetails.getUser());

    return ResponseEntity.ok().build();

  }

  // 비밀번호 변경
  @PostMapping("/change-password")
  public ResponseEntity<?> changePassword(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @Valid @RequestBody ChangePasswordRequest request) {

    log.info("Received request: {}", request);
    log.info("changePassword 요청 도착!"); // 요청이 컨트롤러까지 도달하는지 확인
    log.info("요청한 사용자: {}", userDetails != null ? userDetails.getUsername() : "NULL");
    log.info("입력된 현재 비밀번호: {}", request.getCurrentPassword());

    assert userDetails != null;
    authService.changePassword(userDetails.getUser(), request);

    return ResponseEntity.ok().build();
  }

  // 프로필 조회
  @GetMapping("/profile")
  public ResponseEntity<UserProfileResponse> getProfile(
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    UserProfileResponse profile = userService.getUserProfile(userDetails.getUser().getId());
    return ResponseEntity.ok(profile);
  }

  // 닉네임 변경
  @PatchMapping("/profile/nickname")
  public ResponseEntity<String> updateNickname(
      @Valid @RequestBody UpdateNicknameRequest request,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {

    userService.updateNickname(userDetails.getUser(), request.getNickname());
    return ResponseEntity.ok().build();
  }

  // 한줄 소개 변경
  @PatchMapping("/profile/introduce")
  public ResponseEntity<String> updateIntroduce(
      @Valid @RequestBody UpdateIntroduceRequest request,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {

    userService.updateIntroduce(userDetails.getUser(), request.getIntroduce());
    return ResponseEntity.ok().build();
  }


}

