package com.finflow.ai.module.auth.dto;

import com.finflow.ai.module.user.dto.UserResponse;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UserResponse user;
}
