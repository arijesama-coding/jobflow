package com.jobflow.service.impl;

import com.jobflow.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around JavaMailSender so callers never touch mail
 * infrastructure directly. Every method degrades to a log line instead of
 * throwing if sending fails — auth flows (register, forgot-password) must
 * not break just because SMTP is unreachable in a given environment.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendWelcomeEmail(String to, String firstName) {
        send(to, "Welcome to JobFlow", "Hi " + firstName + ",\n\nYour JobFlow account is ready. Good luck with the search!");
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        send(to, "Verify your JobFlow email",
                "Confirm your email by opening: /verify-email?token=" + token);
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        send(to, "Reset your JobFlow password",
                "Reset your password here: /reset-password?token=" + token
                        + "\n\nIf you didn't request this, you can ignore this email.");
    }

    @Override
    public void sendInterviewReminder(String to, String jobTitle, String companyName, String whenText) {
        send(to, "Interview reminder: " + jobTitle + " at " + companyName,
                "Reminder: your interview for " + jobTitle + " at " + companyName + " is " + whenText + ".");
    }

    @Override
    public void sendFollowUpReminder(String to, String jobTitle, String companyName) {
        send(to, "Follow-up due: " + jobTitle + " at " + companyName,
                "It's time to follow up on your application for " + jobTitle + " at " + companyName + ".");
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send email to {} (subject: {}): {}", to, subject, ex.getMessage());
        }
    }
}
