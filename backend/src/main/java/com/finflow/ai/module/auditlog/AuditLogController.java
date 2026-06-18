package com.finflow.ai.module.auditlog;

import com.finflow.ai.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit-logs")
@PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public ApiResponse<List<AuditLog>> getAllLogs() {
        return ApiResponse.success(auditLogService.getAllLogs(), "Audit logs retrieved successfully");
    }
}
