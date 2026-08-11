package com.jobflow.service.impl;

import com.jobflow.dto.request.*;
import com.jobflow.dto.response.AuthResponse;
import com.jobflow.dto.response.UserResponse;
import com.jobflow.entity.*;
import com.jobflow.exception.AccountLockedException;
import com.jobflow.exception.InvalidTokenException;
import com.jobflow.repository.EmailVerificationTokenRepository;
import com.jobflow.repository.PasswordResetTokenRepository;
import com.jobflow.repository.RefreshTokenRepository;
import com.jobflow.repository.UserRepository;
import com.jobflow.security.jwt.JwtService;
import com.jobflow.service.AuditLogService;
import com.jobflow.service.AuthService;
import com.jobflow.service.EmailService;
import com.jobflow.util.AuditAction;
import com.jobflow.util.SecureTokenGenerator;
import com.jobflow.util.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    @Value("${jobflow.jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Value("${jobflow.security.max-failed-attempts}")
    private int maxFailedAttempts;

    @Value("${jobflow.security.lockout-duration-minutes}")
    private long lockoutDurationMinutes;

    @Value("${jobflow.tokens.email-verification-expiration-minutes}")
    private long emailVerificationExpirationMinutes;

    @Value("${jobflow.tokens.password-reset-expiration-minutes}")
    private long passwordResetExpirationMinutes;

    // ===================== REGISTER =====================

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
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

        issueEmailVerificationToken(user);
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        auditLogService.log(user, AuditAction.REGISTER, "USER", user.getId(), ipAddress);

        return buildAuthResponse(user);
    }

    // ===================== LOGIN =====================

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        autoUnlockIfExpired(user);

        if (isLocked(user)) {
            throw new AccountLockedException(
                    "Account locked until " + user.getLockedUntil().format(DateTimeFormatter.ofPattern("HH:mm")) +
                            " after too many failed attempts");
        }

        if (!user.isActive()) {
            throw new AccountLockedException("This account has been deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            registerFailedAttempt(user, ipAddress);
            throw new BadCredentialsException("Invalid email or password");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        auditLogService.log(user, AuditAction.LOGIN, "USER", user.getId(), ipAddress);

        return buildAuthResponse(user);
    }

    private boolean isLocked(User user) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now());
    }

    private void autoUnlockIfExpired(User user) {
        if (user.getLockedUntil() != null && !user.getLockedUntil().isAfter(LocalDateTime.now())) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
        }
    }

    private void registerFailedAttempt(User user, String ipAddress) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxFailedAttempts) {
            user.setLockedUntil(LocalDateTime.now().plus(lockoutDurationMinutes, ChronoUnit.MINUTES));
            auditLogService.log(user, AuditAction.ACCOUNT_LOCKED, "USER", user.getId(), ipAddress);
        }

        userRepository.save(user);
        auditLogService.log(user, AuditAction.LOGIN_FAILED, "USER", user.getId(), ipAddress);
    }

    // ===================== REFRESH / LOGOUT =====================

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request, String ipAddress) {
        String hash = TokenHasher.sha256(request.getRefreshToken());

        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (existing.isRevoked() || existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expired or already used");
        }

        // Rotation: revoke the used token so it can never be replayed.
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        User user = existing.getUser();
        auditLogService.log(user, AuditAction.TOKEN_REFRESHED, "USER", user.getId(), ipAddress);

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken, String ipAddress) {
        String hash = TokenHasher.sha256(refreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            auditLogService.log(token.getUser(), AuditAction.LOGOUT, "USER", token.getUser().getId(), ipAddress);
        });
    }

    // ===================== PASSWORD RESET =====================

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request, String ipAddress) {
        // Always behave the same way whether or not the email exists, so the
        // endpoint can't be used to enumerate registered accounts.
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String rawToken = SecureTokenGenerator.generate();
            PasswordResetToken token = PasswordResetToken.builder()
                    .user(user)
                    .token(rawToken)
                    .expiresAt(LocalDateTime.now().plus(passwordResetExpirationMinutes, ChronoUnit.MINUTES))
                    .build();
            passwordResetTokenRepository.save(token);

            emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
            auditLogService.log(user, AuditAction.PASSWORD_RESET_REQUESTED, "USER", user.getId(), ipAddress);
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request, String ipAddress) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (token.isUsed() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Invalid or expired reset token");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        token.setUsed(true);
        passwordResetTokenRepository.save(token);

        // Force re-login on every device once the password changes.
        refreshTokenRepository.revokeAllForUser(user.getId());

        auditLogService.log(user, AuditAction.PASSWORD_CHANGED, "USER", user.getId(), ipAddress);
    }

    // ===================== EMAIL VERIFICATION =====================

    @Override
    @Transactional
    public void verifyEmail(String token, String ipAddress) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired verification token"));

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Invalid or expired verification token");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        auditLogService.log(user, AuditAction.EMAIL_VERIFIED, "USER", user.getId(), ipAddress);
    }

    private void issueEmailVerificationToken(User user) {
        String rawToken = SecureTokenGenerator.generate();
        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .token(rawToken)
                .expiresAt(LocalDateTime.now().plus(emailVerificationExpirationMinutes, ChronoUnit.MINUTES))
                .build();
        emailVerificationTokenRepository.save(token);
        emailService.sendVerificationEmail(user.getEmail(), rawToken);
    }

    // ===================== SHARED =====================

    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        String accessToken = jwtService.generateAccessToken(userDetails, Map.of("role", user.getRole().name()));
        String rawRefreshToken = issueRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
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

    private String issueRefreshToken(User user) {
        String rawToken = SecureTokenGenerator.generate();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(TokenHasher.sha256(rawToken))
                .expiresAt(LocalDateTime.now().plus(refreshExpirationMs, ChronoUnit.MILLIS))
                .build();
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }
}
