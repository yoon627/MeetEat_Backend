package com.zb.meeteat.domain.user.service;

import com.zb.meeteat.domain.user.dto.ApiResponse;
import com.zb.meeteat.domain.user.dto.SignupRequestDto;
import com.zb.meeteat.domain.user.dto.SignupResponseDto;
import com.zb.meeteat.domain.user.entity.Role;
import com.zb.meeteat.domain.user.entity.SignUpType;
import com.zb.meeteat.domain.user.entity.User;
import com.zb.meeteat.domain.user.repository.UserRepository;
import com.zb.meeteat.global.exception.CustomException;
import com.zb.meeteat.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiResponse<SignupResponseDto> signup(SignupRequestDto requestDto) {
        validateDuplicateUser(requestDto.getEmail(), requestDto.getNickname());

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = User.builder()
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .nickname(requestDto.getNickname())
                .role(Role.USER)
                .signupType(SignUpType.EMAIL)
                .build();

        userRepository.save(user);

        return ApiResponse.success(new SignupResponseDto(user.getEmail(), user.getNickname())
                , "회원가입이 성공적으로 완료되었습니다.");
    }

    private void validateDuplicateUser(String email, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_REGISTERED);
        }
    }

}

