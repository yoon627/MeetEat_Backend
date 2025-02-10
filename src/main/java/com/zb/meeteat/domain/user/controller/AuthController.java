package com.zb.meeteat.domain.user.controller;

import com.zb.meeteat.domain.user.dto.SigninRequestDto;
import com.zb.meeteat.domain.user.dto.SigninResponseDto;
import com.zb.meeteat.domain.user.dto.SignupRequestDto;
import com.zb.meeteat.domain.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        authService.signup(requestDto);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/signin")
    public ResponseEntity<SigninResponseDto> signin(@Valid @RequestBody SigninRequestDto requestDto) {
        return ResponseEntity.ok(authService.signin(requestDto));
    }


}
