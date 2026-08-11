package com.jobflow.service;

import com.jobflow.dto.request.LoginRequest;
import com.jobflow.dto.request.RegisterRequest;
import com.jobflow.entity.Role;
import com.jobflow.entity.User;
import com.jobflow.exception.AccountLockedException;
import com.jobflow.repository.EmailVerificationTokenRepository;
import com.jobflow.repository.PasswordResetTokenRepository;
import com.jobflow.repository.RefreshTokenRepository;
import com.jobflow.repository.UserRepository;
import com.jobflow.security.jwt.JwtService;
import com.jobflow.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the account-lockout and duplicate-registration rules.
 * Refresh-token rotation and email/reset-token flows are covered separately
 * (integration-level, with Testcontainers) — see the Phase 13 test suite.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private EmailService emailService;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshExpirationMs", 604_800_000L);
        ReflectionTestUtils.setField(authService, "maxFailedAttempts", 5);
        ReflectionTestUtils.setField(authService, "lockoutDurationMinutes", 15L);
        ReflectionTestUtils.setField(authService, "emailVerificationExpirationMinutes", 1440L);
        ReflectionTestUtils.setField(authService, "passwordResetExpirationMinutes", 60L);

        user = User.builder()
                .id(UUID.randomUUID())
                .email("candidate@example.com")
                .passwordHash("hashed")
                .firstName("Ada")
                .lastName("Lovelace")
                .role(Role.USER)
                .failedLoginAttempts(0)
                .active(true)
                .build();
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        RegisterRequest request = new RegisterRequest();
        request.setEmail(user.getEmail());
        request.setPassword("password123");
        request.setFirstName("Ada");
        request.setLastName("Lovelace");

        assertThatThrownBy(() -> authService.register(request, "127.0.0.1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_locksAccountAfterMaxFailedAttempts() {
        user.setFailedLoginAttempts(4); // one more failure should trip the lock
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), eq("hashed"))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        LoginRequest request = new LoginRequest();
        request.setEmail(user.getEmail());
        request.setPassword("wrong-password");

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(user.getLockedUntil()).isNotNull();
        assertThat(user.getLockedUntil()).isAfter(LocalDateTime.now());
        verify(auditLogService).log(eq(user), eq("ACCOUNT_LOCKED"), eq("USER"), any(), eq("127.0.0.1"));
    }

    @Test
    void login_rejectsWhileAccountIsLocked() {
        user.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest();
        request.setEmail(user.getEmail());
        request.setPassword("whatever");

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(AccountLockedException.class);

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_autoUnlocksOnceLockoutWindowHasPassed() {
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1)); // expired lock
        user.setFailedLoginAttempts(5);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), eq("hashed"))).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(any(), any())).thenReturn("access-token");

        LoginRequest request = new LoginRequest();
        request.setEmail(user.getEmail());
        request.setPassword("correct-password");

        authService.login(request, "127.0.0.1");

        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
    }
}
