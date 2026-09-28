package com.finflow.ai.security;

import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.user.Role;
import com.finflow.ai.module.user.User;
import com.finflow.ai.module.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 3600000; // 1 hour
    private final long refreshExpirationMs = 86400000; // 24 hours

    private User testUser;
    private UserPrincipal userPrincipal;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", secret);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", expirationMs);
        ReflectionTestUtils.setField(tokenProvider, "jwtRefreshExpirationMs", refreshExpirationMs);

        Company company = Company.builder().id(1L).name("Acme Corp").taxId("TAX123").build();
        testUser = User.builder()
                .id(10L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .company(company)
                .build();

        userPrincipal = new UserPrincipal(testUser);
        authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
    }

    @Test
    @DisplayName("Should generate valid access token with claims")
    void testGenerateAccessToken() {
        String token = tokenProvider.generateAccessToken(authentication);

        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.getUsernameFromJWT(token)).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void testGenerateRefreshToken() {
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        assertThat(refreshToken).isNotBlank();
        assertThat(tokenProvider.validateToken(refreshToken)).isTrue();
        assertThat(tokenProvider.getUsernameFromJWT(refreshToken)).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should validate token and return false for invalid or tempered token")
    void testValidateInvalidToken() {
        String invalidToken = "invalid.jwt.token";
        assertThat(tokenProvider.validateToken(invalidToken)).isFalse();
    }

    @Test
    @DisplayName("Should return false for expired token")
    void testExpiredToken() {
        // Generate token with negative expiration
        String expiredToken = tokenProvider.generateToken("test@example.com", -1000, Map.of());
        assertThat(tokenProvider.validateToken(expiredToken)).isFalse();
    }
}
