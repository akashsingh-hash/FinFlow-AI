package com.finflow.ai.module.user;

import com.finflow.ai.exception.BusinessException;
import com.finflow.ai.exception.ResourceNotFoundException;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.company.CompanyRepository;
import com.finflow.ai.module.user.dto.InviteEmployeeRequest;
import com.finflow.ai.module.user.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse inviteEmployee(InviteEmployeeRequest request, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("User with this email already exists");
        }

        Company company = admin.getCompany();
        if (company == null) {
            throw new BusinessException("Admin does not belong to any company");
        }

        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        }

        // Create user with temporary password and status PENDING
        String tempPassword = "password123";
        User employee = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .status(UserStatus.PENDING)
                .company(company)
                .manager(manager)
                .department(request.getDepartment())
                .build();

        User saved = userRepository.save(employee);
        auditLogService.log("INVITE_EMPLOYEE", "User", saved.getId(), null, saved);

        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserStatus oldStatus = user.getStatus();
        user.setStatus(UserStatus.ACTIVE);
        User saved = userRepository.save(user);

        auditLogService.log("ACTIVATE_USER", "User", saved.getId(), oldStatus.name(), saved.getStatus().name());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserStatus oldStatus = user.getStatus();
        user.setStatus(UserStatus.INACTIVE);
        User saved = userRepository.save(user);

        auditLogService.log("DEACTIVATE_USER", "User", saved.getId(), oldStatus.name(), saved.getStatus().name());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse assignRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Role oldRole = user.getRole();
        user.setRole(role);
        User saved = userRepository.save(user);

        auditLogService.log("ASSIGN_ROLE", "User", saved.getId(), oldRole.name(), saved.getRole().name());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getCompanyEmployees(Long companyId) {
        return userRepository.findByCompanyId(companyId).stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toResponse(user);
    }
}
