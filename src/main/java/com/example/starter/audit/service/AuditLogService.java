package com.example.starter.audit.service;

import com.example.starter.audit.entity.AuditLog;
import com.example.starter.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(AuditLog log) {
        if (log.getEventTime() == null) {
            log.setEventTime(Instant.now());
        }
        auditLogRepository.save(log);
    }
}
