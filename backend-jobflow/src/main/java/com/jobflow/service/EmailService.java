package com.jobflow.service;

public interface EmailService {
    void sendWelcomeEmail(String to, String firstName);
    void sendVerificationEmail(String to, String token);
    void sendPasswordResetEmail(String to, String token);
    void sendInterviewReminder(String to, String jobTitle, String companyName, String whenText);
    void sendFollowUpReminder(String to, String jobTitle, String companyName);
}
