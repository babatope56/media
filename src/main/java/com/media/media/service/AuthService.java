package com.media.media.service;

import com.media.media.dto.AuthResponse;
import com.media.media.dto.ForgotPasswordRequest;
import com.media.media.dto.LoginRequest;
import com.media.media.dto.PasswordResetResponse;
import com.media.media.dto.ResetPasswordRequest;
import com.media.media.dto.SignUpRequest;

public interface AuthService {
    AuthResponse signUp(SignUpRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse logout(String token);
    PasswordResetResponse forgotPassword(ForgotPasswordRequest request);
    PasswordResetResponse resetPassword(ResetPasswordRequest request);
}
