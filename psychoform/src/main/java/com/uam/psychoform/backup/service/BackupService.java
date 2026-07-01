package com.uam.psychoform.backup.service;

import com.uam.psychoform.academic.repository.ParticipanteRepository;
import com.uam.psychoform.assessment.repository.SesionAplicacionRepository;
import com.uam.psychoform.audit.model.Auditoria;
import com.uam.psychoform.audit.repository.AuditoriaRepository;
import com.uam.psychoform.audit.service.AuditLogService;
import com.uam.psychoform.audit.service.AuditLogService.AuditEvent;
import com.uam.psychoform.reporting.repository.ReporteGeneradoRepository;
import com.uam.psychoform.security.SecurityPermissions;
import jakarta.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BackupService {
    private static final String ENTITY = "respaldo_exportacion";

    private final ParticipanteRepository participantes;
    private final SesionAplicacionRepository sesiones;
    private final ReporteGeneradoRepository reportes;
    private final AuditoriaRepository auditoria;
    private final AuditLogService audit;
    private final Clock clock;
    private final Path backupRoot;

    public BackupService(ParticipanteRepository participantes, SesionAplicacionRepository sesiones,
            ReporteGeneradoRepository reportes, AuditoriaRepository auditoria, AuditLogService audit, Clock clock,
            @Value("${bfa.backup.root:./storage/backups}") String backupRoot) {
        this.participantes = participantes;
        this.sesiones = sesiones;
        this.reportes = reportes;
        this.auditoria = auditoria;
        this.audit = audit;
        this.clock = clock;
        this.backupRoot = Path.of(backupRoot);
    }

    @PreAuthorize(SecurityPermissions.REPORTE_EXPORTAR)
    public List<BackupView> list() {
        return auditoria.findAllByOrderByCreadoEnDesc().stream()
                .filter(a -> ENTITY.equals(a.getEntidad()) && "RESPALDO_GENERADO".equals(a.getAccion()))
                .map(this::toView)
                .toList();
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.REPORTE_EXPORTAR)
    public BackupView generate() {
        try {
            Files.createDirectories(backupRoot);
            String stamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now(clock));
            String fileName = "bfa-backup-" + stamp + ".json";
            Path file = backupRoot.resolve(fileName);
            String payload = "{"
                    + "\"generatedAt\":\"" + LocalDateTime.now(clock) + "\","
                    + "\"participants\":" + participantes.count() + ","
                    + "\"sessions\":" + sesiones.count() + ","
                    + "\"reports\":" + reportes.count() + ","
                    + "\"auditRows\":" + auditoria.count()
                    + "}";
            Files.writeString(file, payload, StandardCharsets.UTF_8);
            long size = Files.size(file);
            Auditoria row = audit.recordTrusted(new AuditEvent("RESPALDO_GENERADO", ENTITY, fileName, null,
                    "{\"fileName\":\"" + json(fileName) + "\",\"path\":\"" + json(file.toString())
                            + "\",\"sizeBytes\":" + size + "}",
                    null, null));
            return new BackupView(fileName, file.toString(), size, row.getCreadoEn(), "GENERADO");
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el respaldo", ex);
        }
    }

    @PreAuthorize(SecurityPermissions.REPORTE_EXPORTAR)
    public BackupFile download(String fileName) {
        Path file = backupRoot.resolve(fileName).normalize();
        if (!file.startsWith(backupRoot.normalize()) || !Files.exists(file)) {
            throw new EntityNotFoundException("Respaldo no encontrado: " + fileName);
        }
        try {
            return new BackupFile(file.getFileName().toString(), Files.readAllBytes(file));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo leer el respaldo", ex);
        }
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.REPORTE_EXPORTAR)
    public RestoreRequestView requestRestore(String fileName) {
        download(fileName);
        Auditoria row = audit.recordTrusted(new AuditEvent("RESPALDO_RESTAURACION_SOLICITADA", ENTITY, fileName, null,
                "{\"fileName\":\"" + json(fileName) + "\"}", null, null));
        return new RestoreRequestView(fileName, row.getId(), row.getCreadoEn());
    }

    private BackupView toView(Auditoria row) {
        String json = row.getValorNuevo() == null ? "" : row.getValorNuevo();
        String fileName = value(json, "fileName", row.getEntidadId());
        String path = value(json, "path", "");
        long size = number(json, "sizeBytes", 0L);
        return new BackupView(fileName, path, size, row.getCreadoEn(), "GENERADO");
    }

    private static String value(String json, String key, String fallback) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : fallback;
    }

    private static long number(String json, String key, long fallback) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)").matcher(json);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : fallback;
    }

    private static String json(String value) {
        return (value == null ? "" : value).replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public record BackupView(String fileName, String path, long sizeBytes, LocalDateTime generatedAt, String status) {
    }

    public record BackupFile(String fileName, byte[] bytes) {
    }

    public record RestoreRequestView(String fileName, long auditId, LocalDateTime requestedAt) {
    }
}
