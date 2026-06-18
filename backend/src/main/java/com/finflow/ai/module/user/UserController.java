package com.finflow.ai.module.user;

import com.finflow.ai.common.ApiResponse;
import com.finflow.ai.module.user.dto.InviteEmployeeRequest;
import com.finflow.ai.module.user.dto.UserResponse;
import com.finflow.ai.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/invite")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    public ApiResponse<UserResponse> inviteEmployee(
            @Valid @RequestBody InviteEmployeeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        UserResponse response = userService.inviteEmployee(request, principal.getUsername());
        return ApiResponse.success(response, "Employee invited successfully");
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    public ApiResponse<UserResponse> activateUser(@PathVariable Long id) {
        UserResponse response = userService.activateUser(id);
        return ApiResponse.success(response, "User account activated successfully");
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    public ApiResponse<UserResponse> deactivateUser(@PathVariable Long id) {
        UserResponse response = userService.deactivateUser(id);
        return ApiResponse.success(response, "User account deactivated successfully");
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> assignRole(@PathVariable Long id, @RequestParam Role role) {
        UserResponse response = userService.assignRole(id, role);
        return ApiResponse.success(response, "User role updated successfully");
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getCompanyEmployees(@AuthenticationPrincipal UserPrincipal principal) {
        Long companyId = principal.getUser().getCompany().getId();
        List<UserResponse> responses = userService.getCompanyEmployees(companyId);
        return ApiResponse.success(responses, "Company directory retrieved successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ApiResponse.success(response, "User profile retrieved successfully");
    }
}
