package com.uam.psychoform.security.service;

import com.uam.psychoform.audit.service.AuditLogService;
import com.uam.psychoform.audit.service.AuditLogService.AuditEvent;
import com.uam.psychoform.security.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PasswordResetRequestService {
    private final UsuarioRepository usuarios;
    private final AuditLogService audit;
    private final Clock clock;
    private final Path outboxRoot;

    public PasswordResetRequestService(UsuarioRepository usuarios, AuditLogService audit, Clock clock,
            @Value("${bfa.notification.outbox-root:./storage/outbox}") String outboxRoot) {
        this.usuarios = usuarios;
        this.audit = audit;
        this.clock = clock;
        this.outboxRoot = Path.of(outboxRoot);
    }

    @Transactional
    public ResetRequestView request(String usernameOrEmail) {
        String normalized = usernameOrEmail == null ? "" : usernameOrEmail.trim();
        UUID requestId = UUID.randomUUID();
        String userId = usuarios.findByNombreUsuario(normalized).map(u -> String.valueOf(u.getId())).orElse(null);
        try {
            Files.createDirectories(outboxRoot);
            String stamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now(clock));
            Path file = outboxRoot.resolve("password-reset-" + requestId + "-" + stamp + ".json");
            String payload = "{\"type\":\"PASSWORD_RESET_REQUEST\",\"requestId\":\"" + requestId
                    + "\",\"usernameOrEmail\":\"" + json(normalized) + "\",\"userId\":"
                    + (userId == null ? "null" : "\"" + userId + "\"") + "}";
            Files.writeString(file, payload, StandardCharsets.UTF_8);
            audit.recordTrusted(new AuditEvent("PASSWORD_RESET_SOLICITADO", "usuario", userId, null,
                    "{\"requestId\":\"" + requestId + "\",\"file\":\"" + json(file.toString()) + "\"}",
                    null, null));
            return new ResetRequestView(requestId, file.getFileName().toString(), userId != null);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo registrar la solicitud de recuperacion", ex);
        }
    }

    private static String json(String value) {
        return (value == null ? "" : value).replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public record ResetRequestView(UUID requestId, String fileName, boolean matchedUser) {
    }
}
