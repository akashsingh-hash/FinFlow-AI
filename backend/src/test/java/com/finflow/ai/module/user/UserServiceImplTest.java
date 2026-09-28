package com.finflow.ai.module.user;

import com.finflow.ai.exception.BusinessException;
import com.finflow.ai.exception.ResourceNotFoundException;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.user.dto.InviteEmployeeRequest;
import com.finflow.ai.module.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private Company company;
    private User admin;
    private User employee;
    private UserResponse userResponse;
    private InviteEmployeeRequest inviteRequest;

    @BeforeEach
    void setUp() {
        company = Company.builder().id(1L).name("Acme Corp").build();

        admin = User.builder()
                .id(1L)
                .email("admin@acme.com")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .company(company)
                .build();

        employee = User.builder()
                .id(2L)
                .email("emp@acme.com")
                .firstName("Jane")
                .lastName("Doe")
                .role(Role.EMPLOYEE)
                .status(UserStatus.PENDING)
                .company(company)
                .department("Sales")
                .build();

        userResponse = UserResponse.builder()
                .id(2L)
                .email("emp@acme.com")
                .role(Role.EMPLOYEE)
                .status(UserStatus.PENDING)
                .build();

        inviteRequest = InviteEmployeeRequest.builder()
                .email("emp@acme.com")
                .firstName("Jane")
                .lastName("Doe")
                .role(Role.EMPLOYEE)
                .department("Sales")
                .build();
    }

    @Test
    @DisplayName("Should invite employee successfully and assign company and PENDING status")
    void testInviteEmployeeSuccess() {
        when(userRepository.findByEmail("admin@acme.com")).thenReturn(Optional.of(admin));
        when(userRepository.existsByEmail("emp@acme.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashedTempPassword");
        when(userRepository.save(any(User.class))).thenReturn(employee);
        when(userMapper.toResponse(employee)).thenReturn(userResponse);

        UserResponse result = userService.inviteEmployee(inviteRequest, "admin@acme.com");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("emp@acme.com");
        verify(userRepository).save(any(User.class));
        verify(auditLogService).log(eq("INVITE_EMPLOYEE"), eq("User"), eq(2L), eq(null), any());
    }

    @Test
    @DisplayName("Should throw BusinessException when inviting user with existing email")
    void testInviteEmployeeDuplicateEmail() {
        when(userRepository.findByEmail("admin@acme.com")).thenReturn(Optional.of(admin));
        when(userRepository.existsByEmail("emp@acme.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.inviteEmployee(inviteRequest, "admin@acme.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User with this email already exists");
    }

    @Test
    @DisplayName("Should activate user successfully")
    void testActivateUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(userRepository.save(employee)).thenReturn(employee);
        when(userMapper.toResponse(employee)).thenReturn(
                UserResponse.builder().id(2L).status(UserStatus.ACTIVE).build()
        );

        UserResponse response = userService.activateUser(2L);

        assertThat(employee.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(auditLogService).log(eq("ACTIVATE_USER"), eq("User"), eq(2L), eq("PENDING"), eq("ACTIVE"));
    }

    @Test
    @DisplayName("Should deactivate user successfully")
    void testDeactivateUser() {
        employee.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(userRepository.save(employee)).thenReturn(employee);
        when(userMapper.toResponse(employee)).thenReturn(
                UserResponse.builder().id(2L).status(UserStatus.INACTIVE).build()
        );

        UserResponse response = userService.deactivateUser(2L);

        assertThat(employee.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(auditLogService).log(eq("DEACTIVATE_USER"), eq("User"), eq(2L), eq("ACTIVE"), eq("INACTIVE"));
    }

    @Test
    @DisplayName("Should change user role")
    void testAssignRole() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(userRepository.save(employee)).thenReturn(employee);
        when(userMapper.toResponse(employee)).thenReturn(
                UserResponse.builder().id(2L).role(Role.MANAGER).build()
        );

        UserResponse response = userService.assignRole(2L, Role.MANAGER);

        assertThat(employee.getRole()).isEqualTo(Role.MANAGER);
        verify(auditLogService).log(eq("ASSIGN_ROLE"), eq("User"), eq(2L), eq("EMPLOYEE"), eq("MANAGER"));
    }
}
