package com.media.media.service.impl;

import com.media.media.dto.AuthResponse;
import com.media.media.dto.ForgotPasswordRequest;
import com.media.media.dto.LoginRequest;
import com.media.media.dto.PasswordResetResponse;
import com.media.media.dto.ResetPasswordRequest;
import com.media.media.dto.SignUpRequest;
import com.media.media.model.PasswordResetToken;
import com.media.media.model.User;
import com.media.media.repository.PasswordResetTokenRepository;
import com.media.media.repository.UserRepository;
import com.media.media.security.JwtTokenProvider;
import com.media.media.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final long PASSWORD_RESET_TTL_MS = 15 * 60 * 1000;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public AuthResponse signUp(SignUpRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return new AuthResponse(null, null, null, "Username already taken");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse(null, null, null, "Email already registered");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        User savedUser = userRepository.save(user);
        
        // Generate JWT token
        String token = jwtTokenProvider.generateToken(savedUser.getUsername());
        
        return new AuthResponse(token, savedUser.getUsername(), savedUser.getEmail(), "Sign up successful");
    }
    
    @Override
    public AuthResponse login(LoginRequest request) {
        // Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);
        
        if (user == null) {
            return new AuthResponse(null, null, null, "Invalid username or password");
        }
        
        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new AuthResponse(null, null, null, "Invalid username or password");
        }
        
        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getUsername());
        
        return new AuthResponse(token, user.getUsername(), user.getEmail(), "Login successful");
    }
    
    @Override
    public AuthResponse logout(String token) {
        // In a token-based system, logout is typically handled client-side by discarding the token
        // This is a placeholder for additional logout logic if needed (e.g., token blacklisting)
        return new AuthResponse(null, null, null, "Logout successful");
    }

    @Override
    public PasswordResetResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            return new PasswordResetResponse(
                    "If an account exists for that email, reset instructions have been generated",
                    null
            );
        }

        passwordResetTokenRepository.deleteByUserAndUsedAtIsNull(user);

        String rawToken = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setTokenHash(hashToken(rawToken));
        passwordResetToken.setExpiresAt(System.currentTimeMillis() + PASSWORD_RESET_TTL_MS);
        passwordResetTokenRepository.save(passwordResetToken);

        return new PasswordResetResponse(
                "Reset token created. Use it to choose a new password.",
                rawToken
        );
    }

    @Override
    public PasswordResetResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByTokenHashAndUsedAtIsNullAndExpiresAtGreaterThan(
                        hashToken(request.getToken()),
                        System.currentTimeMillis()
                )
                .orElse(null);

        if (passwordResetToken == null) {
            return new PasswordResetResponse("Reset token is invalid or expired", null);
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        passwordResetToken.setUsedAt(System.currentTimeMillis());
        passwordResetTokenRepository.save(passwordResetToken);

        return new PasswordResetResponse("Password reset successful", null);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
