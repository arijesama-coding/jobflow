package com.jobflow.service;

import com.jobflow.entity.User;

import java.util.UUID;

public interface AuditLogService {
    void log(User user, String action, String entity, UUID entityId, String ipAddress);
}
