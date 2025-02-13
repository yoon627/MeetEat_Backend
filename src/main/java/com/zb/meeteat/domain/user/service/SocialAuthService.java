package com.zb.meeteat.domain.user.service;

import com.zb.meeteat.domain.user.dto.AuthCodeResponseDto;
import com.zb.meeteat.domain.user.entity.User;
import com.zb.meeteat.domain.user.repository.UserRepository;
import com.zb.meeteat.external.kakao.KakaoAuthClient;
import com.zb.meeteat.external.kakao.KakaoTokenResponse;
import com.zb.meeteat.external.kakao.KakaoUserResponse;
import com.zb.meeteat.external.naver.NaverAuthClient;
import com.zb.meeteat.external.naver.NaverTokenResponse;
import com.zb.meeteat.external.naver.NaverUserResponse;
import com.zb.meeteat.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SocialAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final KakaoAuthClient kakaoAuthClient;
    private final NaverAuthClient naverAuthClient;

    @Transactional
    public AuthCodeResponseDto socialSignin(String provider, String authCode) {
        if ("kakao".equalsIgnoreCase(provider)) {
            return processKakaoSignin(authCode);
        } else if ("naver".equalsIgnoreCase(provider)) {
            return processNaverSignin(authCode);
        } else {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다.");
        }
    }


    public AuthCodeResponseDto processKakaoSignin(String authCode) {

        try {
            log.info("받은 authCode: {}", authCode);

            // 카카오에서 토큰 가져오기
            KakaoTokenResponse tokenResponse = kakaoAuthClient.getToken(authCode);
            if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                throw new IllegalArgumentException("카카오 액세스 토큰을 가져올 수 없습니다.");
            }
            log.info("받은 카카오 액세스 토큰: {}", tokenResponse.getAccessToken());

            // 토큰을 이용해 사용자 정보 가져오기
            KakaoUserResponse userResponse = kakaoAuthClient.getUserInfo(tokenResponse.getAccessToken());
            if (userResponse == null || userResponse.getKakaoAccount() == null) {
                throw new IllegalArgumentException("카카오 사용자 정보를 가져올 수 없습니다.");
            }
            log.info("받은 카카오 사용자 정보: {}", userResponse);

            // 이메일 기준으로 사용자 확인 (기존 회원 목록에 없으면 새로 생성)
            String email = userResponse.getKakaoAccount().getEmail();
            if (email == null) {
                throw new IllegalArgumentException("카카오 사용자 이메일 정보가 없습니다.");
            }

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        log.info("신규 카카오 사용자 저장: {}", email);
                        return userRepository.save(User.ofKakao(userResponse));
                    });


            // JWT 발급
            String jwtToken = jwtUtil.generateToken(user);
            log.info("JWT 토큰 발급: {}", jwtToken);

            // 응답 DTO 반환
            return new AuthCodeResponseDto(jwtToken, user.isProfileIncomplete());

        } catch (Exception e) {
            log.error("카카오 로그인 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("카카오 로그인 중 오류 발생: " + e.getMessage());
        }
    }

    private AuthCodeResponseDto processNaverSignin(String authCode) {
        try {
            log.info("받은 네이버 authCode: {}", authCode);

            // 네이버에서 토큰 가져오기
            NaverTokenResponse tokenResponse = naverAuthClient.getToken(authCode);
            if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                throw new IllegalArgumentException("네이버 액세스 토큰을 가져올 수 없습니다.");
            }
            log.info("받은 네이버 액세스 토큰: {}", tokenResponse.getAccessToken());

            // 토큰을 이용해 사용자 정보 가져오기
            NaverUserResponse userResponse = naverAuthClient.getUserInfo(tokenResponse.getAccessToken());
            if (userResponse == null || userResponse.getResponse() == null) {
                throw new IllegalArgumentException("네이버 사용자 정보를 가져올 수 없습니다.");
            }
            log.info("받은 네이버 사용자 정보: {}", userResponse);

            // 이메일 기준으로 사용자 확인 (기존 회원 목록에 없으면 새로 생성)
            String email = userResponse.getResponse().getEmail();
            if (email == null) {
                throw new IllegalArgumentException("네이버 사용자 이메일 정보가 없습니다.");
            }

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        log.info("신규 네이버 사용자 저장: {}", email);
                        return userRepository.save(User.ofNaver(userResponse));
                    });

            // JWT 발급
            String jwtToken = jwtUtil.generateToken(user);
            log.info("JWT 토큰 발급: {}", jwtToken);

            // 응답 DTO 반환
            return new AuthCodeResponseDto(jwtToken, user.isProfileIncomplete());
        } catch (Exception e) {
            log.error("네이버 로그인 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("네이버 로그인 중 오류 발생: " + e.getMessage());
        }

    }
}
