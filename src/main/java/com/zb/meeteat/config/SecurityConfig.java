package com.zb.meeteat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 보안 설정을 위한 클래스 비밀번호 암호화를 위해 BCryptPasswordEncoder를 빈으로 등록
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
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

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors
            .configurationSource(corsConfigurationSource())
        )
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/**", "/**/**").permitAll());
    return http.build();
  }

  /**
   * Cors 정책 설정
   *
   * @return CorsConfigurationSource
   */
  @Bean
  protected CorsConfigurationSource corsConfigurationSource() {

    CorsConfiguration corsConfigurationV1 = new CorsConfiguration();
    corsConfigurationV1.addAllowedOrigin("*"); // 명확한 Origin 명시
    corsConfigurationV1.addAllowedOriginPattern(
        "http://localhost:5173/**"); // 명확한 Origin 명시
    corsConfigurationV1.addAllowedOriginPattern(
        "https://meet-eat-frontend.vercel.app/**"); // 명확한 Origin 명시
//    corsConfigurationV1.setAllowCredentials(true);
    corsConfigurationV1.addAllowedMethod("*");
    corsConfigurationV1.addAllowedHeader("*");

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfigurationV1);
    source.registerCorsConfiguration("/api/**", corsConfigurationV1);

    return source;
  }
}
