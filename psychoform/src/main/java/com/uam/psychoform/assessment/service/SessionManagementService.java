package com.uam.psychoform.assessment.service;

import com.uam.psychoform.assessment.model.*;
import com.uam.psychoform.assessment.repository.*;
import com.uam.psychoform.instrument.model.EstadoVersionTest;
import com.uam.psychoform.instrument.model.Subtest;
import com.uam.psychoform.instrument.model.VersionTest;
import com.uam.psychoform.instrument.repository.VersionTestRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SessionManagementService {
    private final SesionAplicacionRepository sesiones;
    private final SesionSubtestRepository sesionSubtests;
    private final VersionTestRepository versiones;
    private final UsuarioRepository usuarios;
    private final CurrentActor currentActor;
    private final EntityManager entityManager;
    private final Clock clock;

    public SessionManagementService(SesionAplicacionRepository sesiones, SesionSubtestRepository sesionSubtests,
            VersionTestRepository versiones, UsuarioRepository usuarios, CurrentActor currentActor,
            EntityManager entityManager, Clock clock) {
        this.sesiones = sesiones;
        this.sesionSubtests = sesionSubtests;
        this.versiones = versiones;
        this.usuarios = usuarios;
        this.currentActor = currentActor;
        this.entityManager = entityManager;
        this.clock = clock;
    }

    @PreAuthorize("hasAuthority('PERM_SESION_APLICAR') or hasAuthority('PERM_SESION_CREAR')")
    public List<SesionAplicacion> listSessions() {
        return sesiones.findAll();
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_SESION_CREAR')")
    public SesionAplicacion create(CreateSessionCommand command) {
        VersionTest version = versiones.findById(command.versionTestId())
                .orElseThrow(() -> new EntityNotFoundException("Version no encontrada: " + command.versionTestId()));
        if (version.getEstado() != EstadoVersionTest.PUBLICADO) {
            throw new IllegalStateException("La sesion requiere una version publicada");
        }
        UUID actorId = currentActor.usuarioId();
        Usuario actor = usuarios.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado: " + actorId));
        SesionAplicacion session = new SesionAplicacion();
        session.setVersionTest(version);
        session.setCodigoSesion(command.code());
        session.setNombreSesion(command.name());
        session.setDescripcion(command.description());
        session.setInicioProgramado(command.scheduledStart());
        session.setFinProgramado(command.scheduledEnd());
        session.setUbicacion(command.location());
        session.setEstado(EstadoSesionAplicacion.PROGRAMADA);
        session.setCreadoPor(actor);
        session.setCreadoEn(LocalDateTime.now(clock));
        return sesiones.save(session);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_SESION_CREAR')")
    public List<SesionSubtest> replaceSubtests(long sessionId, List<SessionSubtestCommand> commands) {
        SesionAplicacion session = sesiones.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Sesion no encontrada: " + sessionId));
        if (session.getEstado() != EstadoSesionAplicacion.PROGRAMADA) {
            throw new IllegalStateException("Solo se configuran subtests en sesiones programadas");
        }
        return commands.stream().map(command -> saveSubtest(session, command)).toList();
    }

    private SesionSubtest saveSubtest(SesionAplicacion session, SessionSubtestCommand command) {
        Subtest subtest = entityManager.find(Subtest.class, command.subtestId());
        if (subtest == null) {
            throw new EntityNotFoundException("Subtest no encontrado: " + command.subtestId());
        }
        if (!subtest.getVersionTest().getId().equals(session.getVersionTest().getId())) {
            throw new IllegalArgumentException("El subtest no pertenece a la version de la sesion");
        }
        SesionSubtest row = sesionSubtests.findBySesionAplicacionIdAndSubtestId(session.getId(), subtest.getId())
                .orElseGet(SesionSubtest::new);
        row.setSesionAplicacion(session);
        row.setVersionTest(session.getVersionTest());
        row.setSubtest(subtest);
        row.setNumeroOrden(command.order());
        row.setTiempoLimiteSegundos(command.timeLimitSeconds());
        row.setPermiteAleatorizarItems(Boolean.TRUE.equals(command.randomizeItems()));
        row.setPermiteAleatorizarOpciones(Boolean.TRUE.equals(command.randomizeOptions()));
        return sesionSubtests.save(row);
    }

    public record CreateSessionCommand(Long versionTestId, String code, String name, String description,
            LocalDateTime scheduledStart, LocalDateTime scheduledEnd, String location) {
    }

    public record SessionSubtestCommand(Long subtestId, Integer order, Integer timeLimitSeconds,
            Boolean randomizeItems, Boolean randomizeOptions) {
    }
}

