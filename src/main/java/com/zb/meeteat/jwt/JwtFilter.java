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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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

        // 3. SecurityContext에 인증 정보 저장
        UserDetails userDetails = new UserDetailsImpl(user);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }

    chain.doFilter(request, response);
  }
}
