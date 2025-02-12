package com.zb.meeteat.domain.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SignupRequestDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * 기본 유효한 회원가입 요청을 생성하는 메서드
     */
    private SignupRequestDto createValidSignupRequest() {
        return SignupRequestDto.builder()
                .email("valid@example.com")
                .password("Password123!")
                .nickname("nickname")
                .build();
    }

    /**
     * 커스텀 필드 값을 설정할 수 있는 빌더 메서드
     */
    private SignupRequestDto.SignupRequestDtoBuilder baseSignupRequestBuilder() {
        return SignupRequestDto.builder()
                .email("valid@example.com")
                .password("Password123!")
                .nickname("nickname");
    }

    @Test
    @DisplayName("유효한 회원가입 요청은 검증 오류가 발생하지 않아야 한다")
    void validSignupRequest_shouldPassValidation() {
        // given
        SignupRequestDto requestDto = createValidSignupRequest();

        // when
        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(requestDto);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 검증 실패")
    void invalidEmail_shouldFailValidation() {
        // given
        SignupRequestDto requestDto = baseSignupRequestBuilder()
                .email("invalid-email") // 잘못된 이메일 형식
                .build();

        // when
        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(requestDto);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("유효한 이메일 형식을 입력하세요."));
    }

    @Test
    @DisplayName("비밀번호가 누락되면 검증 실패")
    void missingPassword_shouldFailValidation() {
        // given
        SignupRequestDto requestDto = baseSignupRequestBuilder()
                .password(null) // 비밀번호 누락
                .build();

        // when
        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(requestDto);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("비밀번호는 필수 입력 항목입니다."));
    }

    @Test
    @DisplayName("닉네임이 누락되면 검증 실패")
    void missingNickname_shouldFailValidation() {
        // given
        SignupRequestDto requestDto = baseSignupRequestBuilder()
                .nickname(null) // 닉네임 누락
                .build();

        // when
        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(requestDto);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("닉네임은 필수 입력 항목입니다."));
    }

    @Test
    @DisplayName("비밀번호가 정규식을 충족하지 않으면 검증 실패")
    void invalidPassword_shouldFailValidation() {
        // given
        SignupRequestDto requestDto = baseSignupRequestBuilder()
                .password("password") // 정규식 조건 미충족 (특수문자 없음)
                .build();

        // when
        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(requestDto);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("비밀번호는 최소 8자 이상이며, 영문, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."));
    }
}
