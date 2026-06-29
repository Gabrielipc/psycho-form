package com.uam.psychoform.reporting.service;

import com.uam.psychoform.audit.service.AuditLogService;
import com.uam.psychoform.audit.service.AuditLogService.AuditEvent;
import com.uam.psychoform.security.SecurityPermissions;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ResultNotificationService {
    private final ResultQueryService results;
    private final AuditLogService audit;
    private final Clock clock;
    private final Path outboxRoot;

    public ResultNotificationService(ResultQueryService results, AuditLogService audit, Clock clock,
            @Value("${bfa.notification.outbox-root:./storage/outbox}") String outboxRoot) {
        this.results = results;
        this.audit = audit;
        this.clock = clock;
        this.outboxRoot = Path.of(outboxRoot);
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.RESULTADO_VER)
    public NotificationView sendResult(long attemptId) {
        var result = results.getAttemptResult(attemptId);
        try {
            Files.createDirectories(outboxRoot);
            String stamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now(clock));
            Path file = outboxRoot.resolve("result-" + attemptId + "-" + stamp + ".json");
            String payload = "{\"type\":\"RESULT_NOTIFICATION\",\"attemptId\":" + attemptId
                    + ",\"resultId\":" + result.resultId()
                    + ",\"totalScore\":\"" + json(result.totalScore()) + "\""
                    + ",\"disclaimer\":\"" + json(result.disclaimer()) + "\"}";
            Files.writeString(file, payload, StandardCharsets.UTF_8);
            audit.recordTrusted(new AuditEvent("RESULTADO_NOTIFICACION_ENCOLADA", "intento_test",
                    String.valueOf(attemptId), null,
                    "{\"file\":\"" + json(file.toString()) + "\",\"resultId\":" + result.resultId() + "}",
                    null, null));
            return new NotificationView(attemptId, result.resultId(), file.getFileName().toString(), file.toString(),
                    "OUTBOX");
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo encolar la notificacion del resultado", ex);
        }
    }

    private static String json(Object value) {
        return String.valueOf(value == null ? "" : value).replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public record NotificationView(long attemptId, long resultId, String fileName, String path, String provider) {
    }
}
