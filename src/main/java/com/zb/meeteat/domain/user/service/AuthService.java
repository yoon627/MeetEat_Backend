package com.zb.meeteat.domain.user.service;

import com.zb.meeteat.domain.user.dto.AuthCodeResponseDto;
import com.zb.meeteat.domain.user.dto.ChangePasswordRequest;
import com.zb.meeteat.domain.user.dto.SigninRequestDto;
import com.zb.meeteat.domain.user.dto.SignupRequestDto;
import com.zb.meeteat.domain.user.entity.Role;
import com.zb.meeteat.domain.user.entity.SignUpType;
import com.zb.meeteat.domain.user.entity.User;
import com.zb.meeteat.domain.user.repository.UserRepository;
import com.zb.meeteat.exception.UserCustomException;
import com.zb.meeteat.exception.UserErrorCode;
import com.zb.meeteat.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
    }

    @Transactional
    public AuthCodeResponseDto signin(SigninRequestDto request) {
        // 1. 이메일로 사용자 찾기
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserCustomException(UserErrorCode.USER_NOT_FOUND));

        // 2. 탈퇴 예정 계정인지 확인 (`isPenalty`가 true면 탈퇴 예정 상태)
        if (user.getIsPenalty()) {
            throw new UserCustomException(UserErrorCode.USER_SCHEDULED_FOR_DELETION);
        }

        // 3. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserCustomException(UserErrorCode.INVALID_CREDENTIALS);
        }

        // 4. JWT 토큰 생성
        String accessToken = jwtUtil.generateToken(user);

        // 5. 프로필 업데이트 필요 여부 확인 (닉네임이 없거나 비어있는 경우 true)
        boolean needProfileUpdate = user.isProfileIncomplete();
        return new AuthCodeResponseDto(accessToken, needProfileUpdate);
    }

    @Transactional
    public void signout(String token) {
        String jwt = token.replace("Bearer ", "");

        if (!jwtUtil.validateToken(jwt)) {
            throw new UserCustomException(UserErrorCode.INVALID_TOKEN);
        }

        jwtUtil.blacklistToken(jwt);
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        // 1. 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UserCustomException(UserErrorCode.PASSWORD_MISMATCH);
        }

        // 2. 새 비밀번호가 현재 비밀번호와 동일한지 체크
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new UserCustomException(UserErrorCode.SAME_PASSWORD);
        }

        // 3. 비밀번호 변경 후 저장
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

}

