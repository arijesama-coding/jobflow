package com.jobflow.service.impl;

import com.jobflow.entity.AuditLog;
import com.jobflow.entity.User;
import com.jobflow.repository.AuditLogRepository;
import com.jobflow.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * REQUIRES_NEW so an audit entry is still written even if the calling
     * transaction later rolls back (e.g. a failed login attempt).
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User user, String action, String entity, UUID entityId, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(auditLog);
    }
}
