package com.nines.nutsfact.config;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nines.nutsfact.domain.model.auth.AuthToken;
import com.nines.nutsfact.domain.model.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    public JwtUtil(
            @Value("${jwt.secret:nutsfact-default-secret-key-for-development-only-change-in-production}") String secret,
            @Value("${jwt.access-token-expiration:3600}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration:604800}") long refreshTokenExpiration) {
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            keyBytes = paddedKey;
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationSeconds = accessTokenExpiration;
        this.refreshTokenExpirationSeconds = refreshTokenExpiration;
    }

    public AuthToken generateTokens(User user) {
        Instant now = Instant.now();
        Instant accessExpiry = now.plusSeconds(accessTokenExpirationSeconds);
        Instant refreshExpiry = now.plusSeconds(refreshTokenExpirationSeconds);

        String accessToken = Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("email", user.getEmail())
                .claim("businessAccountId", user.getBusinessAccountId())
                .claim("role", user.getRole())
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(accessExpiry))
                .signWith(secretKey)
                .compact();

        String refreshToken = Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(refreshExpiry))
                .signWith(secretKey)
                .compact();

        return AuthToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpirationSeconds)
                .expiresAt(accessExpiry.getEpochSecond())
                .build();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        return claims.getExpiration().after(new Date());
    }

    public Integer getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return Integer.parseInt(claims.getSubject());
    }

    public String getTokenType(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("type", String.class);
    }

    public boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }
}
