package com.zb.meeteat.domain.user.service;

import com.zb.meeteat.domain.user.dto.SignupRequestDto;
import com.zb.meeteat.domain.user.entity.Role;
import com.zb.meeteat.domain.user.entity.SignUpType;
import com.zb.meeteat.domain.user.entity.User;
import com.zb.meeteat.domain.user.repository.UserRepository;
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

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 회원가입 요청을 위한 기본 DTO 생성 메서드
     */
    private SignupRequestDto createSignupRequestDto(String email, String password, String nickname) {
        return SignupRequestDto.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .build();
    }

    @Test
    @DisplayName("성공적인 회원가입 테스트")
    void signup_success() {
        // given
        SignupRequestDto requestDto = createSignupRequestDto("test@example.com", "Password123!", "nickname");

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(userRepository.existsByNickname(requestDto.getNickname())).thenReturn(false);
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User savedUser = userService.signup(requestDto);

        // then
        assertThat(savedUser.getEmail()).isEqualTo(requestDto.getEmail());
        assertThat(savedUser.getNickname()).isEqualTo(requestDto.getNickname());
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword"); // 비밀번호 암호화 확인
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(savedUser.getSignupType()).isEqualTo(SignUpType.EMAIL);
    }

    @Test
    @DisplayName("이메일 중복으로 인한 회원가입 실패")
    void signupEmailDuplicateFail() {
        // given
        SignupRequestDto requestDto = createSignupRequestDto("duplicate@example.com", "Password123!", "nickname");

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> userService.signup(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
    }

    @Test
    @DisplayName("닉네임 중복으로 인한 회원가입 실패")
    void signupNicknameDuplicateFail() {
        // given
        SignupRequestDto requestDto = createSignupRequestDto("test@example.com", "Password123!", "duplicateNickname");

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(userRepository.existsByNickname(requestDto.getNickname())).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> userService.signup(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 닉네임입니다.");
    }
}
