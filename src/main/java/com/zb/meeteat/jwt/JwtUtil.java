package com.zb.meeteat.jwt;

import com.zb.meeteat.domain.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24시간 유지

    public JwtUtil(@Value("${spring.jwt.secret}") String secretKey) {
        try {
            // Base64 디코딩 시도 (getBytes 제거)
            byte[] decodedKey = Base64.getDecoder().decode(secretKey);
            this.key = Keys.hmacShaKeyFor(decodedKey);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid Base64-encoded secret key. Please check your environment variable.", e);
        }
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

}
