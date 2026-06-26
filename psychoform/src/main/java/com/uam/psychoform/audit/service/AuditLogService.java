package com.uam.psychoform.audit.service;

import com.uam.psychoform.audit.model.Auditoria;
import com.uam.psychoform.audit.repository.AuditoriaRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.SecurityPermissions;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditLogService {
    private final AuditoriaRepository repository;
    private final UsuarioRepository usuarios;
    private final CurrentActor currentActor;
    private final Clock clock;

    public AuditLogService(AuditoriaRepository repository, UsuarioRepository usuarios, CurrentActor currentActor,
            Clock clock) {
        this.repository = repository;
        this.usuarios = usuarios;
        this.currentActor = currentActor;
        this.clock = clock;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.AUDITORIA_REGISTRAR)
    public Auditoria record(AuditEvent event) {
        Auditoria audit = new Auditoria();
        audit.setUsuario(resolveCurrentActorOrNull());
        audit.setAccion(event.action());
        audit.setEntidad(event.entity());
        audit.setEntidadId(event.entityId());
        audit.setValorAnterior(event.previousValuesJson());
        audit.setValorNuevo(event.newValuesJson());
        audit.setDireccionIp(event.ip());
        audit.setAgenteUsuario(event.userAgent());
        audit.setCreadoEn(LocalDateTime.now(clock));
        repository.save(audit);
        return audit;
    }

    @PreAuthorize(SecurityPermissions.AUDITORIA_VER)
    public List<Auditoria> listByEntity(String entity, String entityId) {
        return repository.findByEntidadAndEntidadIdOrderByCreadoEnDesc(entity, entityId);
    }

    private Usuario resolveCurrentActorOrNull() {
        try {
            return usuarios.findById(currentActor.usuarioId()).orElse(null);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public record AuditEvent(String action, String entity, String entityId, String previousValuesJson,
            String newValuesJson, String ip, String userAgent) {
    }
}
