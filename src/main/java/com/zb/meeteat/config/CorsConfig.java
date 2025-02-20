package com.zb.meeteat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();

    // 모든 출처에서 요청 허용 (보안상 운영에서는 특정 출처만 허용해야 함)
    config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000", "https://meet--eat.com"));

    // 모든 HTTP 메서드 허용 (GET, POST, PUT, DELETE 등)
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH" ));

    // 허용할 헤더를 특정 값으로 제한
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

    // 자격 증명 허용 (Authorization 헤더 포함)
    config.setAllowCredentials(true);

    // 모든 경로에 대해 설정 적용
    source.registerCorsConfiguration("/**", config);

    return new CorsFilter(source);
  }
}
