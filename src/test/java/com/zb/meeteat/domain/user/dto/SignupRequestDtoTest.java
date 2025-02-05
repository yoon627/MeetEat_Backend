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
        // ValidatorFactory를 통해 Validator 인스턴스 생성
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("유효한 회원가입 요청은 검증 오류가 발생하지 않아야 한다")
    void validSignupRequest_shouldPassValidation() {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .email("valid@example.com")
                .password("Password123!")
                .nickname("nickname")
                .build();

        // when
        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(requestDto);

        // then
        assertThat(violations).isEmpty();

    }

    @Test
    @DisplayName("유효하지 않은 이메일 형식은 검증 오류가 발생해야 한다")
    void invalidEmailTest() {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .email("invalid-email")
                .password("ValidPass123")
                .nickname("ValidNickname")
                .build();

        // when
        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(requestDto);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("유효한 이메일 형식을 입력하세요.");

    }

    @Test
    @DisplayName("비밀번호가 누락되면 검증 오류가 발생해야 한다")
    void missingPasswordTest() {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .email("valid@example.com")
                .password("")
                .nickname("ValidNickname")
                .build();

        // when
        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(requestDto);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("비밀번호는 필수 입력 항목입니다.");

    }

    @Test
    @DisplayName("닉네임이 누락되면 검증 오류가 발생해야 한다")
    void missingNicknameTest() {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .email("valid@example.com")
                .password("ValidPass123")
                .nickname("")
                .build();

        // when
        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(requestDto);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("닉네임은 필수 입력 항목입니다.");

    }

    @Test
    @DisplayName("비밀번호가 정규식을 충족하지 않으면 검증 오류가 발생해야 한다")
    void invalidPassword_shouldFailValidation() {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .email("user@example.com")
                .password("password")
                .nickname("nickname")
                .build();

        // when
        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(requestDto);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("비밀번호는 최소 8자 이상이며, 영문, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다.");

    }
}
