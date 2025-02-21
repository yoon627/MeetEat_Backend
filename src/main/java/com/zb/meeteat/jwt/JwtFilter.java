package com.zb.meeteat.jwt;

import com.zb.meeteat.domain.user.entity.User;
import com.zb.meeteat.domain.user.repository.UserRepository;
import com.zb.meeteat.exception.CustomException;
import com.zb.meeteat.exception.ErrorCode;
import com.zb.meeteat.security.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain)
      throws ServletException, IOException {

    String token = request.getHeader("Authorization");
    log.info("token: {}", token);
    if (token != null && token.startsWith("Bearer ")) {
      String jwt = token.replace("Bearer ", "");

      // 1. 블랙리스트 체크
      if (jwtUtil.isBlacklisted(jwt)) {
        throw new CustomException(ErrorCode.INVALID_TOKEN);
      }

      // 2. JWT 유효성 검사
      if (jwtUtil.validateToken(jwt)) {
        Long userId = jwtUtil.getUserId(jwt); // 토큰에서 사용자 ID 가져오기
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        log.info("token validated: {}", token);

        // 3. SecurityContext에 인증 정보 저장
        UserDetails userDetails = new UserDetailsImpl(user);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        log.info("Authentication before setting: {}",
            SecurityContextHolder.getContext().getAuthentication());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("Authentication after setting: {}",
            SecurityContextHolder.getContext().getAuthentication());
      }
    }

    chain.doFilter(request, response);
  }
}
