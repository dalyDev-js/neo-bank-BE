package com.neobank.service;

import com.neobank.entity.AuditLog;
import com.neobank.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(UUID userId, String action, String entityType,
            UUID entityId, String description) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception ex) {
            // Never let audit logging crash the main flow
            log.error("Failed to save audit log: {}", ex.getMessage());
        }
    }
}