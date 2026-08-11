package com.jobflow.controller;

import com.jobflow.dto.request.*;
import com.jobflow.dto.response.AuthResponse;
import com.jobflow.service.AuthService;
import com.jobflow.util.IpAddressResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest req) {
        return ResponseEntity.ok(authService.register(request, IpAddressResolver.resolve(req)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest req) {
        return ResponseEntity.ok(authService.login(request, IpAddressResolver.resolve(req)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest req) {
        return ResponseEntity.ok(authService.refresh(request, IpAddressResolver.resolve(req)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest req) {
        authService.logout(request.getRefreshToken(), IpAddressResolver.resolve(req));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest req) {
        authService.forgotPassword(request, IpAddressResolver.resolve(req));
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest req) {
        authService.resetPassword(request, IpAddressResolver.resolve(req));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token, HttpServletRequest req) {
        authService.verifyEmail(token, IpAddressResolver.resolve(req));
        return ResponseEntity.ok().build();
    }
}
