package com.smartdocs.aikb.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 令牌签发与解析。
 * <ul>
 *   <li>Access Token：短期（默认 15 min），客户端通过 {@code Authorization: Bearer} 头携带。</li>
 *   <li>Refresh Token：长期（默认 7 天），仅用于换取新的 access。建议放 HttpOnly Cookie。</li>
 * </ul>
 * 每个 Token 携带 {@code jti}（JWT ID），登出时把 jti 写入 Redis 黑名单。
 */
@Slf4j
@Component
public class JwtTokenProvider {

    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";
    public static final String TYPE_ACCESS = "ACCESS";
    public static final String TYPE_REFRESH = "REFRESH";

    private final String secret;
    private final Duration accessTtl;
    private final Duration refreshTtl;
    private SecretKey signingKey;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expire:900}") long accessTtlSeconds,
            @Value("${app.jwt.refresh-token-expire:604800}") long refreshTtlSeconds) {
        this.secret = secret;
        this.accessTtl = Duration.ofSeconds(accessTtlSeconds);
        this.refreshTtl = Duration.ofSeconds(refreshTtlSeconds);
    }

    @PostConstruct
    void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret 长度必须 >= 32 字符");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenPayload issueAccess(Long userId, String username) {
        return issue(userId, username, TYPE_ACCESS, accessTtl);
    }

    public TokenPayload issueRefresh(Long userId, String username) {
        return issue(userId, username, TYPE_REFRESH, refreshTtl);
    }

    private TokenPayload issue(Long userId, String username, String type, Duration ttl) {
        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttl.toMillis());

        String token = Jwts.builder()
                .id(jti)
                .subject(String.valueOf(userId))
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_TOKEN_TYPE, type)
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey)
                .compact();

        return new TokenPayload(token, jti, userId, username, type, exp.toInstant().toEpochMilli());
    }

    /** 解析 Token；签名错误/过期等会抛 {@link io.jsonwebtoken.JwtException}。 */
    public ParsedToken parse(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token);
        Claims claims = jws.getPayload();
        return new ParsedToken(
                claims.getId(),
                Long.parseLong(claims.getSubject()),
                claims.get(CLAIM_USERNAME, String.class),
                claims.get(CLAIM_TOKEN_TYPE, String.class),
                claims.getExpiration().toInstant().toEpochMilli()
        );
    }

    public Duration getAccessTtl() {
        return accessTtl;
    }

    public Duration getRefreshTtl() {
        return refreshTtl;
    }

    /** 已签发但尚未交付的 Token 信息。 */
    public record TokenPayload(String token, String jti, Long userId, String username, String type, long expireAtMillis) {}

    /** 解析后的 Token 内容。 */
    public record ParsedToken(String jti, Long userId, String username, String type, long expireAtMillis) {}
}
