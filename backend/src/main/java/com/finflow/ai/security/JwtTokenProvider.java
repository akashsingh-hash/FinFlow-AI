package com.finflow.ai.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${finflow.jwt.secret}")
    private String jwtSecret;

    @Value("${finflow.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${finflow.jwt.refresh-expiration-ms}")
    private long jwtRefreshExpirationMs;

    private SecretKey getSignKey() {
        // Safe decoding for base64 or raw bytes fallback
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (IllegalArgumentException e) {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateToken(userPrincipal.getUsername(), jwtExpirationMs, Map.of(
                "role", userPrincipal.getUser().getRole().name(),
                "id", userPrincipal.getUser().getId(),
                "companyId", userPrincipal.getUser().getCompany() != null ? userPrincipal.getUser().getCompany().getId() : ""
        ));
    }

    public String generateRefreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateToken(userPrincipal.getUsername(), jwtRefreshExpirationMs, new HashMap<>());
    }

    public String generateToken(String username, long expirationMs, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignKey())
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }
}
