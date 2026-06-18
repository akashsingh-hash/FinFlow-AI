package com.finflow.ai.module.auditlog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public void log(String action, String entityType, Long entityId, Object oldValue, Object newValue) {
        String userEmail = "SYSTEM";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            userEmail = authentication.getName();
        }

        String oldStr = serialize(oldValue);
        String newStr = serialize(newValue);

        AuditLog auditLog = AuditLog.builder()
                .userEmail(userEmail)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldStr)
                .newValue(newStr)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Audit logged: {} on {} (ID: {}) by {}", action, entityType, entityId, userEmail);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    private String serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit log object", e);
            return obj.toString();
        }
    }
}
