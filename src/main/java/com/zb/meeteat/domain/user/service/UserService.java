package com.zb.meeteat.domain.user.service;

import com.zb.meeteat.domain.user.dto.SignupRequestDto;
import com.zb.meeteat.domain.user.entity.Role;
import com.zb.meeteat.domain.user.entity.SignUpType;
import com.zb.meeteat.domain.user.entity.User;
import com.zb.meeteat.domain.user.repository.UserRepository;
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

}

