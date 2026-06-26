package com.uam.psychoform.assessment.service;

import com.uam.psychoform.assessment.model.AsignacionTest;
import com.uam.psychoform.assessment.model.EstadoAsignacion;
import com.uam.psychoform.assessment.model.EstadoSesionAplicacion;
import com.uam.psychoform.assessment.repository.AsignacionTestRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.SecurityPermissions;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ParticipantAccessService {
    private final AsignacionTestRepository asignaciones;
    private final UsuarioRepository usuarios;
    private final ParticipantTokenService tokens;
    private final CurrentActor currentActor;
    private final Clock clock;

    public ParticipantAccessService(AsignacionTestRepository asignaciones, UsuarioRepository usuarios,
            ParticipantTokenService tokens, CurrentActor currentActor, Clock clock) {
        this.asignaciones = asignaciones;
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.currentActor = currentActor;
        this.clock = clock;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.PARTICIPANTE_ACCESO_GESTIONAR)
    public IssuedParticipantToken issueToken(long asignacionId, Duration ttl) {
        UUID actorId = currentActor.usuarioId();
        Usuario evaluador = usuarios.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado: " + actorId));
        AsignacionTest asignacion = asignaciones.findByIdForUpdate(asignacionId)
                .orElseThrow(() -> new EntityNotFoundException("Asignacion no encontrada: " + asignacionId));

        String rawToken = tokens.generateRawToken();
        LocalDateTime expiresAt = LocalDateTime.now(clock).plus(ttl);
        asignacion.setEvaluador(evaluador);
        asignacion.setTokenAccesoHash(tokens.hash(rawToken));
        asignacion.setTokenExpiraEn(expiresAt);
        asignacion.setTokenUsadoEn(null);
        asignacion.setIntentosAcceso(0);
        asignaciones.save(asignacion);
        return new IssuedParticipantToken(asignacion.getId(), rawToken, expiresAt);
    }

    @Transactional
    public ParticipantAccess grantParticipantAccess(long asignacionId, String rawToken) {
        LocalDateTime now = LocalDateTime.now(clock);
        AsignacionTest asignacion = asignaciones.findByIdForUpdate(asignacionId)
                .orElseThrow(() -> new EntityNotFoundException("Asignacion no encontrada: " + asignacionId));

        if (asignacion.getTokenExpiraEn().isBefore(now)) {
            asignacion.setEstado(EstadoAsignacion.EXPIRADO);
            throw new IllegalStateException("Token expirado");
        }
        if (!tokens.matches(rawToken, asignacion.getTokenAccesoHash())) {
            asignacion.setIntentosAcceso(asignacion.getIntentosAcceso() + 1);
            throw new IllegalArgumentException("Token invalido");
        }
        if (asignacion.getSesionAplicacion().getEstado() != EstadoSesionAplicacion.ABIERTA) {
            throw new IllegalStateException("La sesion no esta abierta");
        }

        asignacion.setTokenUsadoEn(now);
        return new ParticipantAccess(asignacion.getId(), asignacion.getParticipante().getId());
    }

    @Transactional
    public ParticipantAccess validateRuntimeAccess(ParticipantAccess tokenAccess) {
        LocalDateTime now = LocalDateTime.now(clock);
        AsignacionTest asignacion = asignaciones.findByIdForUpdate(tokenAccess.assignmentId())
                .orElseThrow(() -> new EntityNotFoundException("Asignacion no encontrada: " + tokenAccess.assignmentId()));

        if (!Objects.equals(asignacion.getParticipante().getId(), tokenAccess.participantId())) {
            throw new org.springframework.security.access.AccessDeniedException("Asignacion fuera de alcance");
        }
        if (asignacion.getTokenExpiraEn().isBefore(now)) {
            asignacion.setEstado(EstadoAsignacion.EXPIRADO);
            throw new IllegalStateException("Token expirado");
        }
        if (asignacion.getSesionAplicacion().getEstado() != EstadoSesionAplicacion.ABIERTA) {
            throw new IllegalStateException("La sesion no esta abierta");
        }
        if (asignacion.getEstado() == EstadoAsignacion.ANULADO || asignacion.getEstado() == EstadoAsignacion.EXPIRADO
                || asignacion.getEstado() == EstadoAsignacion.COMPLETADO) {
            throw new IllegalStateException("La asignacion no permite operaciones de participante");
        }
        return new ParticipantAccess(asignacion.getId(), asignacion.getParticipante().getId());
    }

    public record IssuedParticipantToken(Long assignmentId, String rawToken, LocalDateTime expiresAt) {
    }

    public record ParticipantAccess(Long assignmentId, UUID participantId) {
    }
}
