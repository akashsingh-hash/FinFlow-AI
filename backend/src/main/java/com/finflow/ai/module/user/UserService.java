package com.finflow.ai.module.user;

import com.finflow.ai.module.user.dto.InviteEmployeeRequest;
import com.finflow.ai.module.user.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse inviteEmployee(InviteEmployeeRequest request, String adminEmail);
    UserResponse activateUser(Long id);
    UserResponse deactivateUser(Long id);
    UserResponse assignRole(Long id, Role role);
    List<UserResponse> getCompanyEmployees(Long companyId);
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
}
