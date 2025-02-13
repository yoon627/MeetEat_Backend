package com.zb.meeteat.domain.user.controller;

import com.zb.meeteat.domain.user.dto.AuthCodeResponseDto;
import com.zb.meeteat.domain.user.dto.ChangePasswordRequest;
import com.zb.meeteat.domain.user.dto.SigninRequestDto;
import com.zb.meeteat.domain.user.dto.SignupRequestDto;
import com.zb.meeteat.domain.user.service.AuthService;
import com.zb.meeteat.domain.user.service.SocialAuthService;
import com.zb.meeteat.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SocialAuthService socialAuthService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        authService.signup(requestDto);
        return ResponseEntity.ok("회원가입 성공");
    }

    // 이메일 로그인
    @PostMapping("/signin")
    public ResponseEntity<AuthCodeResponseDto> signin(@Valid @RequestBody SigninRequestDto requestDto) {
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

        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }

}
