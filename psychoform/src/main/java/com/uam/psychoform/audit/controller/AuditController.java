package com.uam.psychoform.audit.controller;

import com.uam.psychoform.audit.service.AuditLogService;
import com.uam.psychoform.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auditoria")
public class AuditController {
    private final AuditLogService audit;

    public AuditController(AuditLogService audit) {
        this.audit = audit;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam String entity, @RequestParam String entityId) {
        return ApiResponse.ok(audit.listByEntity(entity, entityId));
    }
}
