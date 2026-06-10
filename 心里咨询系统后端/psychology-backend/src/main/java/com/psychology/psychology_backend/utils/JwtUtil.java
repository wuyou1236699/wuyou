package com.psychology.psychology_backend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // 密钥至少需要 256 位（32 字节），原占位符已替换为足够长度的字符串
    private final SecretKey key = Keys.hmacShaKeyFor(
            "your-256-bit-secret-key-here-must-be-long-enough-32bytes"
                    .getBytes(StandardCharsets.UTF_8)
    );

    // Token 有效期：7 天
    private final long EXPIRE = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 生成 JWT Token
     * @param userId 用户 ID
     * @param role   用户角色
     * @return JWT Token 字符串
     */
    public String generateToken(Long userId, String role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(key)
                .compact();
    }

    /**
     * 从 Token 中解析用户 ID
     * @param token JWT Token
     * @return 用户 ID，解析失败抛出异常
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从 Token 中解析用户类型
     * @param token JWT Token
     * @return 用户类型（user/counselor/admin）
     */
    public String getUserTypeFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("role", String.class);
    }
}