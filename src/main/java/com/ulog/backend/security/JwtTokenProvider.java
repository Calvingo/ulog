package com.ulog.backend.security;

import com.ulog.backend.common.api.ErrorCode;
import com.ulog.backend.common.exception.ApiException;
import com.ulog.backend.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final Key signingKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        if (properties.getSecret() == null || properties.getSecret().length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters");
        }
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes());
    }

    public String generateAccessToken(Long userId) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.getAccessTokenValidityMinutes() * 60);
        return Jwts.builder()
            .setSubject(String.valueOf(userId))
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiry))
            .claim("type", "access")
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.getRefreshTokenValidityDays() * 24 * 3600);
        return Jwts.builder()
            .setSubject(String.valueOf(userId))
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiry))
            .claim("type", "refresh")
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseClaims(token).getBody().getSubject());
    }

    public String getTokenType(String token) {
        Object type = parseClaims(token).getBody().get("type");
        return type == null ? "access" : type.toString();
    }

    public boolean isExpired(String token) {
        try {
            Date expiration = parseClaims(token).getBody().getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        }
    }

    public Jws<Claims> parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
        } catch (ExpiredJwtException ex) {
            throw new ApiException(ErrorCode.TOKEN_EXPIRED, ErrorCode.TOKEN_EXPIRED.getDefaultMessage());
        } catch (SecurityException | MalformedJwtException | IllegalArgumentException ex) {
            throw new ApiException(ErrorCode.TOKEN_INVALID, ex.getMessage());
        }
    }

    public LocalDateTime getExpiry(String token) {
        Date expiration = parseClaims(token).getBody().getExpiration();
        return LocalDateTime.ofInstant(expiration.toInstant(), ZoneOffset.UTC);
    }
}
