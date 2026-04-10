package com.media.media.controller;

import com.media.media.dto.AuthResponse;
import com.media.media.dto.ForgotPasswordRequest;
import com.media.media.dto.LoginRequest;
import com.media.media.dto.PasswordResetResponse;
import com.media.media.dto.ResetPasswordRequest;
import com.media.media.dto.SignUpRequest;
import com.media.media.security.LoginRateLimiter;
import com.media.media.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter loginRateLimiter;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@RequestBody SignUpRequest request,
                                               HttpServletResponse httpResponse) {
        AuthResponse response = authService.signUp(request);

        if (response.getToken() != null) {
            setJwtCookie(httpResponse, response.getToken());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(stripToken(response));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest,
                                              HttpServletResponse httpResponse) {
        String ip = httpRequest.getRemoteAddr();
        if (loginRateLimiter.isBlocked(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new AuthResponse(null, null, null, "Too many login attempts. Try again in 15 minutes."));
        }

        AuthResponse response = authService.login(request);

        if (response.getToken() != null) {
            loginRateLimiter.clearAttempts(ip);
            setJwtCookie(httpResponse, response.getToken());
            return ResponseEntity.ok(stripToken(response));
        }

        loginRateLimiter.recordAttempt(ip);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @CookieValue(value = "jwt", required = false) String cookieToken,
            HttpServletResponse httpResponse) {

        String token = resolveToken(bearerToken, cookieToken);
        AuthResponse response = authService.logout(token);

        // Clear the cookie by setting max-age to 0
        ResponseCookie cleared = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cleared.toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthResponse response = authService.getCurrentUser(authentication.getName());
        if (response == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        PasswordResetResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        PasswordResetResponse response = authService.resetPassword(request);

        if ("Password reset successful".equals(response.getMessage())) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // -------------------------------------------------------------------------

    private void setJwtCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMillis(jwtExpirationMs))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /** Return a copy of the response with the token field cleared. */
    private AuthResponse stripToken(AuthResponse response) {
        return new AuthResponse(null, response.getUsername(), response.getEmail(), response.getMessage());
    }

    private String resolveToken(String bearerToken, String cookieToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return cookieToken;
    }
}
