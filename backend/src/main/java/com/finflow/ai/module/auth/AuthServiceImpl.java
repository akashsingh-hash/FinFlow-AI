package com.finflow.ai.module.auth;

import com.finflow.ai.exception.BusinessException;
import com.finflow.ai.exception.ResourceNotFoundException;
import com.finflow.ai.exception.UnauthorizedException;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.auth.dto.*;
import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.company.CompanyRepository;
import com.finflow.ai.module.user.*;
import com.finflow.ai.module.user.dto.UserResponse;
import com.finflow.ai.security.JwtTokenProvider;
import com.finflow.ai.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email address already in use.");
        }

        // Handle Company creation/lookup
        Company company = companyRepository.findByName(request.getCompanyName())
                .orElseGet(() -> {
                    log.info("Creating new company: {}", request.getCompanyName());
                    Company newCompany = Company.builder()
                            .name(request.getCompanyName())
                            .taxId(request.getTaxId())
                            .build();
                    return companyRepository.save(newCompany);
                });

        // The registering user becomes the first ADMIN of the company
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .company(company)
                .department(request.getDepartment() != null ? request.getDepartment() : "Administration")
                .build();

        User savedUser = userRepository.save(user);
        auditLogService.log("USER_REGISTER", "User", savedUser.getId(), null, savedUser.getEmail());

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Retrieve user first to ensure they are active
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Your account is " + user.getStatus().name() + ". Please contact your administrator.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        auditLogService.log("USER_LOGIN", "User", user.getId(), null, user.getEmail());

        return LoginResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (tokenProvider.validateToken(refreshToken)) {
            String username = tokenProvider.getUsernameFromJWT(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            UserPrincipal principal = (UserPrincipal) userDetails;
            if (principal.getUser().getStatus() != UserStatus.ACTIVE) {
                throw new BusinessException("User account is inactive.");
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            String newAccessToken = tokenProvider.generateAccessToken(authentication);

            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .user(userMapper.toResponse(principal.getUser()))
                    .build();
        } else {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("Old password does not match current password.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        // If user was PENDING, change to ACTIVE upon password update
        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);

        auditLogService.log("CHANGE_PASSWORD", "User", user.getId(), "PASSWORD_CHANGE_REQUESTED", "SUCCESS");
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Simulate forgot password logic for Phase 1
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            log.info("Simulating forgot password notification for user: {}", user.getEmail());
            auditLogService.log("FORGOT_PASSWORD_REQUEST", "User", user.getId(), null, "PASSWORD_RESET_SIMULATION_TRIGGERED");
        } else {
            log.warn("Forgot password requested for non-existent email: {}", request.getEmail());
        }
    }
}
