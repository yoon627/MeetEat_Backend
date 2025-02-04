package com.zb.meeteat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 보안 설정을 위한 클래스
 * 비밀번호 암호화를 위해 BCryptPasswordEncoder를 빈으로 등록
 */
@Configuration
public class SecurityConfig {

    /**
     * 비밀번호를 안전하게 암호화하기 위한 PasswordEncoder 빈 등록
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
