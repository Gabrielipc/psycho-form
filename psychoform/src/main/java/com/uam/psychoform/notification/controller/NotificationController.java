package com.uam.psychoform.notification.controller;

import com.uam.psychoform.audit.repository.AuditoriaRepository;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.security.SecurityPermissions;
import java.time.LocalDateTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final AuditoriaRepository auditoria;

    public NotificationController(AuditoriaRepository auditoria) {
        this.auditoria = auditoria;
    }

    @GetMapping
    @PreAuthorize(SecurityPermissions.AUTHENTICATED)
    public ApiResponse<?> list() {
        return ApiResponse.ok(auditoria.findAllByOrderByCreadoEnDesc().stream()
                .limit(12)
                .map(a -> new NotificationView(a.getId(), a.getAccion(), a.getEntidad(), a.getEntidadId(), a.getCreadoEn()))
                .toList());
    }

    public record NotificationView(long id, String action, String entity, String entityId, LocalDateTime createdAt) {
    }
}
