package com.zb.meeteat.domain.user.service;

import com.zb.meeteat.domain.user.dto.SignupRequestDto;
import com.zb.meeteat.domain.user.dto.SignupResponseDto;
import com.zb.meeteat.domain.user.entity.Role;
import com.zb.meeteat.domain.user.entity.SignUpType;
import com.zb.meeteat.domain.user.entity.User;
import com.zb.meeteat.domain.user.repository.UserRepository;
import com.zb.meeteat.global.exception.CustomException;
import com.zb.meeteat.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void SetUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("성공적인 회원가입 테스트")
    void signup_success() {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .email("test@example.com")
                .password("Password123!")
                .nickname("nickname")
                .build();

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(userRepository.existsByNickname(requestDto.getNickname())).thenReturn(false);
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(
                new User(requestDto.getEmail(), "encodedPassword", requestDto.getNickname(), null, Role.USER, SignUpType.EMAIL)
        );

        // when
        SignupResponseDto responseDto = (SignupResponseDto) userService.signup(requestDto).getData();

        // then
        assertThat(responseDto.getEmail()).isEqualTo(requestDto.getEmail());
        assertThat(responseDto.getNickname()).isEqualTo(requestDto.getNickname());

    }

    @Test
    @DisplayName("이메일 중복으로 인한 회원가입 실패 테스트")
    void signupEmailDuplicateFail() {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .email("duplicate@example.com")
                .password("Password123!")
                .nickname("nickname")
                .build();

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> userService.signup(requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.EMAIL_ALREADY_REGISTERED.getMessage());

    }

    @Test
    @DisplayName("닉네임 중복으로 인한 회원가입 실패 테스트")
    void signupNicknameDuplicateFail() {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .email("test@example.com")
                .password("Password123!")
                .nickname("duplicateNickname")
                .build();

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(userRepository.existsByNickname(requestDto.getNickname())).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> userService.signup(requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.NICKNAME_ALREADY_REGISTERED.getMessage());
    }


}
