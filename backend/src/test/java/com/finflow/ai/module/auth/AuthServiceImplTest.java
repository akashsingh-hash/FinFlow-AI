package com.finflow.ai.module.auth;

import com.finflow.ai.exception.BusinessException;
import com.finflow.ai.exception.UnauthorizedException;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.auth.dto.LoginRequest;
import com.finflow.ai.module.auth.dto.LoginResponse;
import com.finflow.ai.module.auth.dto.RefreshTokenRequest;
import com.finflow.ai.module.auth.dto.RegisterRequest;
import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.company.CompanyRepository;
import com.finflow.ai.module.user.Role;
import com.finflow.ai.module.user.User;
import com.finflow.ai.module.user.UserMapper;
import com.finflow.ai.module.user.UserRepository;
import com.finflow.ai.module.user.UserStatus;
import com.finflow.ai.module.user.dto.UserResponse;
import com.finflow.ai.security.JwtTokenProvider;
import com.finflow.ai.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Company company;
    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("admin@finflow.com")
                .password("Password123")
                .firstName("John")
                .lastName("Doe")
                .companyName("Acme Corp")
                .taxId("TAX-999")
                .department("Finance")
                .build();

        loginRequest = LoginRequest.builder()
                .email("admin@finflow.com")
                .password("Password123")
                .build();

        company = Company.builder()
                .id(1L)
                .name("Acme Corp")
                .taxId("TAX-999")
                .build();

        user = User.builder()
                .id(10L)
                .email("admin@finflow.com")
                .passwordHash("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .company(company)
                .department("Finance")
                .build();

        userResponse = UserResponse.builder()
                .id(10L)
                .email("admin@finflow.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .companyId(1L)
                .companyName("Acme Corp")
                .build();
    }

    @Test
    @DisplayName("Should successfully register a new user and create company if not existing")
    void testRegisterSuccess() {
        when(userRepository.existsByEmail("admin@finflow.com")).thenReturn(false);
        when(companyRepository.findByName("Acme Corp")).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenReturn(company);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("admin@finflow.com");
        verify(auditLogService).log(eq("USER_REGISTER"), eq("User"), eq(10L), eq(null), eq("admin@finflow.com"));
    }

    @Test
    @DisplayName("Should throw BusinessException when registering with duplicate email")
    void testRegisterDuplicateEmail() {
        when(userRepository.existsByEmail("admin@finflow.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email address already in use");
    }

    @Test
    @DisplayName("Should successfully login active user and return JWT tokens")
    void testLoginSuccess() {
        Authentication auth = new UsernamePasswordAuthenticationToken(new UserPrincipal(user), null);

        when(userRepository.findByEmail("admin@finflow.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenProvider.generateAccessToken(auth)).thenReturn("accessToken123");
        when(tokenProvider.generateRefreshToken(auth)).thenReturn("refreshToken123");
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        LoginResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken123");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken123");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(auditLogService).log(eq("USER_LOGIN"), eq("User"), eq(10L), eq(null), eq("admin@finflow.com"));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when login email is not found")
    void testLoginUserNotFound() {
        when(userRepository.findByEmail("admin@finflow.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Should throw BusinessException when inactive/pending user tries to login")
    void testLoginInactiveUser() {
        user.setStatus(UserStatus.INACTIVE);
        when(userRepository.findByEmail("admin@finflow.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Your account is INACTIVE");
    }

    @Test
    @DisplayName("Should refresh access token with valid refresh token")
    void testRefreshTokenSuccess() {
        RefreshTokenRequest refreshReq = new RefreshTokenRequest("validRefreshToken");
        UserPrincipal principal = new UserPrincipal(user);

        when(tokenProvider.validateToken("validRefreshToken")).thenReturn(true);
        when(tokenProvider.getUsernameFromJWT("validRefreshToken")).thenReturn("admin@finflow.com");
        when(userDetailsService.loadUserByUsername("admin@finflow.com")).thenReturn(principal);
        when(tokenProvider.generateAccessToken(any(Authentication.class))).thenReturn("newAccessToken");
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        LoginResponse response = authService.refresh(refreshReq);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("validRefreshToken");
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when refresh token is invalid")
    void testRefreshTokenInvalid() {
        RefreshTokenRequest refreshReq = new RefreshTokenRequest("invalidToken");
        when(tokenProvider.validateToken("invalidToken")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(refreshReq))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid or expired refresh token");
    }
}
