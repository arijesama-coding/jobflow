package com.jobflow.util;

import jakarta.servlet.http.HttpServletRequest;

public final class IpAddressResolver {
    private IpAddressResolver() {}

    public static String resolve(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
