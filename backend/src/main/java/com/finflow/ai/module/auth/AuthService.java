package com.finflow.ai.module.auth;

import com.finflow.ai.module.auth.dto.*;
import com.finflow.ai.module.user.dto.UserResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    UserResponse register(RegisterRequest request);
    LoginResponse refresh(RefreshTokenRequest request);
    void changePassword(ChangePasswordRequest request, String email);
    void forgotPassword(ForgotPasswordRequest request);
}
