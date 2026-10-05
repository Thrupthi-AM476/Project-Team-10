package com.vps.service;

import com.vps.entity.AuditLog;
import com.vps.entity.User;
import com.vps.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(User user, String action, String entityType, Long entityId, String description) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDescription(description);
        auditLogRepository.save(log);
    }

    public void log(String action, String description) {
        log(null, action, null, null, description);
    }
}
