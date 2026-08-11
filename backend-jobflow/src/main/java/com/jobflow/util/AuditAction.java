package com.jobflow.util;

public final class AuditAction {
    private AuditAction() {}

    public static final String REGISTER = "REGISTER";
    public static final String LOGIN = "LOGIN";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String LOGOUT = "LOGOUT";
    public static final String TOKEN_REFRESHED = "TOKEN_REFRESHED";
    public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";
    public static final String PASSWORD_RESET_REQUESTED = "PASSWORD_RESET_REQUESTED";
    public static final String EMAIL_VERIFIED = "EMAIL_VERIFIED";
    public static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
}
