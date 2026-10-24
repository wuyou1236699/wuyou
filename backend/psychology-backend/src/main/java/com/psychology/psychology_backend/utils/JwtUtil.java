package com.psychology.psychology_backend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /** HMAC-SHA256 至少需要 256 位（32 字节）密钥 */
    private static final int MIN_KEY_BYTES = 32;

    private final SecretKey key;

    /**
     * 签名密钥从配置项 jwt.secret（环境变量 JWT_SECRET）读取，不写在代码里。
     * 未配置或长度不足时，仅为本地演示随机生成一次性密钥：
     * 服务重启后旧 Token 全部失效，生产环境必须配置 JWT_SECRET。
     */
    public JwtUtil(@Value("${jwt.secret:}") String secret) {
        byte[] secretBytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length >= MIN_KEY_BYTES) {
            this.key = Keys.hmacShaKeyFor(secretBytes);
        } else {
            byte[] random = new byte[MIN_KEY_BYTES];
            new SecureRandom().nextBytes(random);
            this.key = Keys.hmacShaKeyFor(random);
            log.warn("未配置有效的 JWT_SECRET（至少 32 字节），已生成临时随机密钥，仅适用于本地演示");
        }
    }

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