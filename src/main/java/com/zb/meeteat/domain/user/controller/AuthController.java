package com.zb.meeteat.domain.user.controller;

import com.zb.meeteat.domain.user.dto.AuthCodeResponseDto;
import com.zb.meeteat.domain.user.dto.SigninRequestDto;
import com.zb.meeteat.domain.user.dto.SignupRequestDto;
import com.zb.meeteat.domain.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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

    @PostMapping("/signin/{provider}")
    public ResponseEntity<AuthCodeResponseDto> socialSignin(
            @PathVariable String provider,
            @RequestBody Map<String, String> requestBody) {

        String authCode = requestBody.get("code");

        log.info("받은 authCode: {}", authCode);

        if (authCode == null || authCode.isEmpty()) {
            throw new IllegalArgumentException("인가 코드가 제공되지 않았습니다.");
        }

        AuthCodeResponseDto response = authService.socialSignin(provider, authCode);
        return ResponseEntity.ok(response);
    }


}
