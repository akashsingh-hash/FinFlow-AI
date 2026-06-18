package com.finflow.ai.module.user.dto;

import com.finflow.ai.module.user.Role;
import com.finflow.ai.module.user.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private UserStatus status;
    private String department;
    private Long companyId;
    private String companyName;
    private Long managerId;
    private String managerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
