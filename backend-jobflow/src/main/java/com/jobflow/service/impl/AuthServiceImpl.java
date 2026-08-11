package com.jobflow.service.impl;

import com.jobflow.dto.request.*;
import com.jobflow.dto.response.AuthResponse;
import com.jobflow.dto.response.UserResponse;
import com.jobflow.entity.Role;
import com.jobflow.entity.User;
import com.jobflow.repository.UserRepository;
import com.jobflow.security.jwt.JwtService;
import com.jobflow.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * NOTE: this is a Phase-1 scaffold implementation covering the happy path only.
 * Still to add per the spec: refresh-token persistence/rotation & revocation,
 * account lockout after repeated failures, email verification + password reset
 * token issuance/consumption, and audit logging on each of these events.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("An account with this email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.USER)
                .build();

        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        // TODO: validate refresh token against refresh_tokens table, rotate it, and reissue.
        throw new UnsupportedOperationException("Refresh token rotation not yet implemented");
    }

    @Override
    public void logout(String refreshToken) {
        // TODO: revoke the refresh token in the refresh_tokens table.
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        // TODO: issue a password_reset_tokens row and send the reset email via EmailService.
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        // TODO: validate token, update password_hash, mark token used.
    }

    @Override
    public void verifyEmail(String token) {
        // TODO: validate email_verification_tokens row and flip is_email_verified.
    }

    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        String accessToken = jwtService.generateAccessToken(userDetails, Map.of("role", user.getRole().name()));

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(null) // TODO: issue and persist a real refresh token
                .tokenType("Bearer")
                .user(UserResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole().name())
                        .build())
                .build();
    }
}
