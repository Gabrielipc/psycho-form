package com.uam.psychoform.assessment.service;

import com.uam.psychoform.academic.model.Participante;
import com.uam.psychoform.academic.repository.ParticipanteRepository;
import com.uam.psychoform.assessment.model.AsignacionTest;
import com.uam.psychoform.assessment.model.EstadoAsignacion;
import com.uam.psychoform.assessment.model.EstadoIntento;
import com.uam.psychoform.assessment.model.EstadoSesionAplicacion;
import com.uam.psychoform.assessment.model.IntentoSubtest;
import com.uam.psychoform.assessment.model.IntentoTest;
import com.uam.psychoform.assessment.model.SesionAplicacion;
import com.uam.psychoform.assessment.model.SesionSubtest;
import com.uam.psychoform.assessment.repository.AsignacionTestRepository;
import com.uam.psychoform.assessment.repository.IntentoSubtestRepository;
import com.uam.psychoform.assessment.repository.IntentoTestRepository;
import com.uam.psychoform.assessment.repository.SesionAplicacionRepository;
import com.uam.psychoform.assessment.repository.SesionSubtestRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.SecurityPermissions;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.assessment.dto.SessionAssignmentDto;
import com.uam.psychoform.audit.service.AuditLogService;
import com.uam.psychoform.security.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ParticipantRuntimeService {
    private final SesionAplicacionRepository sesiones;
    private final SesionSubtestRepository sesionSubtests;
    private final AsignacionTestRepository asignaciones;
    private final IntentoTestRepository intentos;
    private final IntentoSubtestRepository intentoSubtests;
    private final ParticipanteRepository participantes;
    private final UsuarioRepository usuarios;
    private final ParticipantTokenService tokenService;
    private final CurrentActor currentActor;
    private final AuditLogService audit;
    private final Clock clock;

    public ParticipantRuntimeService(SesionAplicacionRepository sesiones, SesionSubtestRepository sesionSubtests,
            AsignacionTestRepository asignaciones, IntentoTestRepository intentos,
            IntentoSubtestRepository intentoSubtests, ParticipanteRepository participantes, UsuarioRepository usuarios,
            ParticipantTokenService tokenService, CurrentActor currentActor, AuditLogService audit, Clock clock) {
        this.sesiones = sesiones;
        this.sesionSubtests = sesionSubtests;
        this.asignaciones = asignaciones;
        this.intentos = intentos;
        this.intentoSubtests = intentoSubtests;
        this.participantes = participantes;
        this.usuarios = usuarios;
        this.tokenService = tokenService;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.SESION_APLICAR)
    public AssignedParticipant assignParticipant(AssignParticipantCommand command) {
        SesionAplicacion sesion = sesiones.findByIdForUpdate(command.sesionAplicacionId())
                .orElseThrow(() -> new EntityNotFoundException("Sesion no encontrada: " + command.sesionAplicacionId()));
        if (sesion.getEstado() == EstadoSesionAplicacion.CERRADA || sesion.getEstado() == EstadoSesionAplicacion.CANCELADA) {
            throw new IllegalStateException("La sesion no permite nuevas asignaciones");
        }
        Participante participante = participantes.findById(command.participanteId())
                .orElseThrow(() -> new EntityNotFoundException("Participante no encontrado: " + command.participanteId()));
        if (asignaciones.findBySesionAplicacionIdAndParticipanteId(sesion.getId(), participante.getId()).isPresent()) {
            throw new IllegalStateException("El participante ya esta asignado a la sesion");
        }
        UUID actorId = currentActor.usuarioId();
        Usuario evaluador = usuarios.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado: " + actorId));
        LocalDateTime now = LocalDateTime.now(clock);
        String rawToken = tokenService.generateRawToken();

        AsignacionTest asignacion = new AsignacionTest();
        asignacion.setSesionAplicacion(sesion);
        asignacion.setParticipante(participante);
        asignacion.setEvaluador(evaluador);
        asignacion.setTokenAccesoHash(tokenService.hash(rawToken));
        asignacion.setTokenExpiraEn(now.plus(command.tokenTtl()));
        asignacion.setIntentosAcceso(0);
        asignacion.setEdadRegistradaAplicacion(ageAt(participante.getFechaNacimiento(), LocalDate.now(clock)));
        asignacion.setSexoAplicacion(participante.getSexo());
        asignacion.setCarreraAplicacion(participante.getCarrera());
        asignacion.setCohorteAplicacion(participante.getCohorte());
        asignacion.setGrupoAcademicoAplicacion(participante.getGrupoAcademico());
        asignacion.setAsignadoEn(now);
        asignacion.setEstado(EstadoAsignacion.ASIGNADO);
        asignaciones.save(asignacion);
        return new AssignedParticipant(asignacion.getId(), rawToken, asignacion.getTokenExpiraEn());
    }

    @Transactional
    public IntentoTest startOrResumeAttempt(ParticipantAccessService.ParticipantAccess access, String deviceInfo,
            String ipAddress) {
        return intentos.findByAsignacionId(access.assignmentId())
                .orElseGet(() -> createAttempt(access.assignmentId(), deviceInfo, ipAddress));
    }

    @Transactional
    public IntentoSubtest startSubtest(ParticipantAccessService.ParticipantAccess access, long intentoId,
            long subtestId) {
        IntentoTest intento = intentos.findByIdForUpdate(intentoId)
                .orElseThrow(() -> new EntityNotFoundException("Intento no encontrado: " + intentoId));
        requireAttemptScope(access, intento);
        if (intento.getEstado() == EstadoIntento.COMPLETADO || intento.getEstado() == EstadoIntento.ANULADO) {
            throw new IllegalStateException("El intento no permite iniciar subtests");
        }
        Long sesionId = intento.getAsignacion().getSesionAplicacion().getId();
        SesionSubtest sesionSubtest = sesionSubtests.findBySesionAplicacionIdAndSubtestId(sesionId, subtestId)
                .orElseThrow(() -> new IllegalStateException("Subtest no habilitado en la sesion"));
        return intentoSubtests.findByIntentoIdAndSubtestId(intentoId, subtestId)
                .orElseGet(() -> createAttemptSubtest(intento, sesionSubtest));
    }

    @Transactional
    public IntentoSubtest finishSubtest(ParticipantAccessService.ParticipantAccess access, long intentoId,
            long subtestId, int tiempoUsadoSegundos) {
        IntentoSubtest intentoSubtest = intentoSubtests.findByIntentoIdAndSubtestId(intentoId, subtestId)
                .orElseThrow(() -> new EntityNotFoundException("Intento de subtest no encontrado"));
        requireAttemptScope(access, intentoSubtest.getIntento());
        if (intentoSubtest.getEstado() != EstadoIntento.EN_PROGRESO) {
            throw new IllegalStateException("El subtest no esta en progreso");
        }
        intentoSubtest.setEstado(EstadoIntento.COMPLETADO);
        intentoSubtest.setFinalizadoEn(LocalDateTime.now(clock));
        intentoSubtest.setTiempoUsadoSegundos(tiempoUsadoSegundos);
        intentoSubtests.save(intentoSubtest);
        return intentoSubtest;
    }

    @Transactional
    public IntentoTest finishAttempt(ParticipantAccessService.ParticipantAccess access, long intentoId,
            int tiempoTotalSegundos) {
        IntentoTest intento = intentos.findByIdForUpdate(intentoId)
                .orElseThrow(() -> new EntityNotFoundException("Intento no encontrado: " + intentoId));
        requireAttemptScope(access, intento);
        if (intento.getEstado() != EstadoIntento.EN_PROGRESO) {
            throw new IllegalStateException("El intento no esta en progreso");
        }
        intento.setEstado(EstadoIntento.COMPLETADO);
        intento.setFinalizadoEn(LocalDateTime.now(clock));
        intento.setUltimaActividadEn(LocalDateTime.now(clock));
        intento.setTiempoTotalSegundos(tiempoTotalSegundos);
        intento.getAsignacion().setEstado(EstadoAsignacion.COMPLETADO);
        intentos.save(intento);
        return intento;
    }

    private static void requireAttemptScope(ParticipantAccessService.ParticipantAccess access, IntentoTest intento) {
        if (!Objects.equals(intento.getAsignacion().getId(), access.assignmentId())) {
            throw new AccessDeniedException("Intento fuera de alcance");
        }
    }

    private IntentoTest createAttempt(long asignacionId, String deviceInfo, String ipAddress) {
        AsignacionTest asignacion = asignaciones.findByIdForUpdate(asignacionId)
                .orElseThrow(() -> new EntityNotFoundException("Asignacion no encontrada: " + asignacionId));
        if (asignacion.getEstado() == EstadoAsignacion.COMPLETADO || asignacion.getEstado() == EstadoAsignacion.ANULADO
                || asignacion.getEstado() == EstadoAsignacion.EXPIRADO) {
            throw new IllegalStateException("La asignacion no permite iniciar intento");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        asignacion.setEstado(EstadoAsignacion.EN_PROGRESO);
        IntentoTest intento = new IntentoTest();
        intento.setAsignacion(asignacion);
        intento.setIniciadoEn(now);
        intento.setUltimaActividadEn(now);
        intento.setEstado(EstadoIntento.EN_PROGRESO);
        intento.setInformacionDispositivo(deviceInfo);
        intento.setDireccionIp(ipAddress);
        intentos.save(intento);
        return intento;
    }

    private IntentoSubtest createAttemptSubtest(IntentoTest intento, SesionSubtest sesionSubtest) {
        LocalDateTime now = LocalDateTime.now(clock);
        IntentoSubtest intentoSubtest = new IntentoSubtest();
        intentoSubtest.setIntento(intento);
        intentoSubtest.setSesionSubtest(sesionSubtest);
        intentoSubtest.setSubtest(sesionSubtest.getSubtest());
        intentoSubtest.setIniciadoEn(now);
        intentoSubtest.setEstado(EstadoIntento.EN_PROGRESO);
        intento.setUltimoSubtest(sesionSubtest.getSubtest());
        intento.setUltimaActividadEn(now);
        intentoSubtests.save(intentoSubtest);
        return intentoSubtest;
    }

    private static Short ageAt(LocalDate birthDate, LocalDate now) {
        return birthDate == null ? null : (short) Period.between(birthDate, now).getYears();
    }

    public record AssignParticipantCommand(Long sesionAplicacionId, UUID participanteId, Duration tokenTtl) {
    }

    public record AssignedParticipant(Long assignmentId, String rawToken, LocalDateTime expiresAt) {
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.SESION_APLICAR)
    public void revokeAssignment(Long sessionId, Long assignmentId) {
        AsignacionTest assignment = asignaciones.findByIdForUpdate(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Asignacion no encontrada: " + assignmentId));
        if (!assignment.getSesionAplicacion().getId().equals(sessionId)) {
            throw new AccessDeniedException("Asignacion fuera de la sesion indicada");
        }
        assignment.setEstado(EstadoAsignacion.ANULADO);
        intentos.findByAsignacionId(assignmentId).ifPresent(attempt -> {
            if (attempt.getEstado() != EstadoIntento.COMPLETADO) {
                attempt.setEstado(EstadoIntento.ANULADO);
                attempt.setUltimaActividadEn(LocalDateTime.now(clock));
                intentos.save(attempt);
            }
        });
        asignaciones.save(assignment);
        audit.recordTrusted(new AuditLogService.AuditEvent("ASIGNACION_REVOCADA", "asignacion_test",
                String.valueOf(assignmentId), null, "{\"sesionId\":" + sessionId + "}", null, null));
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.SESION_APLICAR)
    public void recordIncidence(Long sessionId, IncidenceCommand command) {
        if (!sesiones.existsById(sessionId)) {
            throw new EntityNotFoundException("Sesion no encontrada: " + sessionId);
        }
        String metadata = "{\"participantId\":\"" + command.participantId() + "\",\"text\":\""
                + command.text().replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
        audit.recordTrusted(new AuditLogService.AuditEvent("INCIDENCIA_SESION", "sesion_aplicacion",
                String.valueOf(sessionId), null, metadata, null, null));
    }

    @Transactional(readOnly = true)
    public List<SessionAssignmentDto> getAssignmentsForSession(Long sessionId) {
        List<AsignacionTest> list = asignaciones.findBySesionAplicacionId(sessionId);
        return list.stream().map(asg -> {
            var attemptOpt = intentos.findByAsignacionId(asg.getId());
            String state = "no-iniciado";
            String status = "GENERADO";
            String currentSubtestId = "—";
            int progress = 0;
            String lastActivity = "Nunca";

            if (attemptOpt.isPresent()) {
                var attempt = attemptOpt.get();
                state = attempt.getEstado().name().toLowerCase();
                if ("en_progreso".equals(state)) {
                    state = "en-progreso";
                }
                
                if (attempt.getEstado() == EstadoIntento.EN_PROGRESO || attempt.getEstado() == EstadoIntento.INTERRUMPIDO) {
                    status = "ACTIVO";
                } else if (attempt.getEstado() == EstadoIntento.COMPLETADO) {
                    status = "VENCIDO";
                } else if (attempt.getEstado() == EstadoIntento.ANULADO) {
                    status = "REVOCADO";
                }

                if (attempt.getUltimoSubtest() != null) {
                    currentSubtestId = attempt.getUltimoSubtest().getCodigoSubtest();
                }

                if (attempt.getEstado() == EstadoIntento.COMPLETADO) {
                    progress = 100;
                } else if (attempt.getEstado() == EstadoIntento.EN_PROGRESO || attempt.getEstado() == EstadoIntento.INTERRUMPIDO) {
                    long totalSubtests = sesionSubtests.findBySesionAplicacionIdOrderByNumeroOrdenAsc(sessionId).size();
                    if (totalSubtests > 0) {
                        long completed = intentoSubtests.findByIntentoId(attempt.getId()).stream()
                            .filter(is -> is.getEstado() == EstadoIntento.COMPLETADO)
                            .count();
                        progress = (int) (completed * 100 / totalSubtests);
                        if (progress == 0 && completed == 0) {
                            progress = 10;
                        }
                    }
                }

                if (attempt.getUltimaActividadEn() != null) {
                    lastActivity = attempt.getUltimaActividadEn().toString();
                }
            } else {
                if (asg.getEstado() == EstadoAsignacion.EXPIRADO) {
                    status = "VENCIDO";
                } else if (asg.getEstado() == EstadoAsignacion.ANULADO) {
                    status = "REVOCADO";
                    state = "anulado";
                }
            }

            return new SessionAssignmentDto(
                asg.getId(),
                attemptOpt.map(IntentoTest::getId).orElse(null),
                asg.getParticipante().getId(),
                asg.getParticipante().getNombres() + " " + asg.getParticipante().getApellidos(),
                asg.getParticipante().getCodigoParticipante(),
                status,
                state,
                currentSubtestId,
                progress,
                lastActivity,
                asg.getTokenExpiraEn().toString()
            );
        }).toList();
    }

    public record IncidenceCommand(java.util.UUID participantId, String text) {
    }
}
