package com.jobflow.service;

import com.jobflow.dto.request.*;
import com.jobflow.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request, String ipAddress);
    AuthResponse login(LoginRequest request, String ipAddress);
    AuthResponse refresh(RefreshTokenRequest request, String ipAddress);
    void logout(String refreshToken, String ipAddress);
    void forgotPassword(ForgotPasswordRequest request, String ipAddress);
    void resetPassword(ResetPasswordRequest request, String ipAddress);
    void verifyEmail(String token, String ipAddress);
}
