package com.example.starter.audit.controller;

import com.example.starter.audit.entity.AuditLog;
import com.example.starter.audit.repository.AuditLogRepository;
import com.example.starter.common.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ApiResponse<List<AuditLog>> findAll() {
        return ApiResponse.ok(auditLogRepository.findAll());
    }
}
