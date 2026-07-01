package com.uam.psychoform.assessment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.uam.psychoform.academic.model.Carrera;
import com.uam.psychoform.academic.model.CatalogoSexo;
import com.uam.psychoform.academic.model.Cohorte;
import com.uam.psychoform.academic.model.GrupoAcademico;
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
import com.uam.psychoform.audit.service.AuditLogService;
import com.uam.psychoform.instrument.model.Subtest;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;

class ParticipantRuntimeServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-24T12:00:00Z"), ZoneOffset.UTC);

    private final SesionAplicacionRepository sesiones = Mockito.mock(SesionAplicacionRepository.class);
    private final SesionSubtestRepository sesionSubtests = Mockito.mock(SesionSubtestRepository.class);
    private final AsignacionTestRepository asignaciones = Mockito.mock(AsignacionTestRepository.class);
    private final IntentoTestRepository intentos = Mockito.mock(IntentoTestRepository.class);
    private final IntentoSubtestRepository intentoSubtests = Mockito.mock(IntentoSubtestRepository.class);
    private final ParticipanteRepository participantes = Mockito.mock(ParticipanteRepository.class);
    private final UsuarioRepository usuarios = Mockito.mock(UsuarioRepository.class);
    private final ParticipantTokenService tokenService = Mockito.mock(ParticipantTokenService.class);
    private final CurrentActor currentActor = Mockito.mock(CurrentActor.class);
    private final AuditLogService audit = Mockito.mock(AuditLogService.class);
    private final ParticipantRuntimeService service = new ParticipantRuntimeService(sesiones, sesionSubtests,
            asignaciones, intentos, intentoSubtests, participantes, usuarios, tokenService, currentActor, audit, CLOCK);

    @Test
    void assignParticipantRequierePermisoSesionAplicar() throws Exception {
        Method method = ParticipantRuntimeService.class.getMethod("assignParticipant",
                ParticipantRuntimeService.AssignParticipantCommand.class);

        assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAuthority('PERM_SESION_APLICAR')");
    }

    @Test
    void operacionesAdministrativasDeSesionRequierenPermisoSesionAplicar() throws Exception {
        assertThat(ParticipantRuntimeService.class.getMethod("revokeAssignment", Long.class, Long.class)
                .getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAuthority('PERM_SESION_APLICAR')");
        assertThat(ParticipantRuntimeService.class.getMethod("recordIncidence",
                Long.class, ParticipantRuntimeService.IncidenceCommand.class)
                .getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAuthority('PERM_SESION_APLICAR')");
    }

    @Test
    void operacionesRuntimeDeParticipanteNoUsanPermisoInternoRespuestaRegistrar() throws Exception {
        assertThat(ParticipantRuntimeService.class.getMethod("startOrResumeAttempt",
                ParticipantAccessService.ParticipantAccess.class, String.class, String.class)
                .getAnnotation(PreAuthorize.class)).isNull();
        assertThat(ParticipantRuntimeService.class.getMethod("startSubtest",
                ParticipantAccessService.ParticipantAccess.class, long.class, long.class)
                .getAnnotation(PreAuthorize.class)).isNull();
        assertThat(ParticipantRuntimeService.class.getMethod("finishSubtest",
                ParticipantAccessService.ParticipantAccess.class, long.class, long.class, int.class)
                .getAnnotation(PreAuthorize.class)).isNull();
        assertThat(ParticipantRuntimeService.class.getMethod("finishAttempt",
                ParticipantAccessService.ParticipantAccess.class, long.class, int.class)
                .getAnnotation(PreAuthorize.class)).isNull();
    }

    @Test
    void assignParticipantGuardaSnapshotDemograficoYTokenHasheado() {
        UUID participanteId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        SesionAplicacion sesion = sesion(EstadoSesionAplicacion.PROGRAMADA);
        Participante participante = participante(participanteId);
        Usuario evaluador = new Usuario();
        evaluador.setId(actorId);
        when(sesiones.findByIdForUpdate(10L)).thenReturn(Optional.of(sesion));
        when(participantes.findById(participanteId)).thenReturn(Optional.of(participante));
        when(asignaciones.findBySesionAplicacionIdAndParticipanteId(10L, participanteId)).thenReturn(Optional.empty());
        when(currentActor.usuarioId()).thenReturn(actorId);
        when(usuarios.findById(actorId)).thenReturn(Optional.of(evaluador));
        when(tokenService.generateRawToken()).thenReturn("raw-token");
        when(tokenService.hash("raw-token")).thenReturn("hashed-token");

        ParticipantRuntimeService.AssignedParticipant assigned = service.assignParticipant(
                new ParticipantRuntimeService.AssignParticipantCommand(10L, participanteId, Duration.ofHours(3)));

        assertThat(assigned.rawToken()).isEqualTo("raw-token");
        assertThat(assigned.expiresAt()).isEqualTo(LocalDateTime.ofInstant(CLOCK.instant().plusSeconds(10_800), ZoneOffset.UTC));
        verify(asignaciones).save(Mockito.argThat(a -> a.getEvaluador() == evaluador
                && a.getTokenAccesoHash().equals("hashed-token")
                && a.getEdadRegistradaAplicacion().equals((short) 26)
                && a.getSexoAplicacion() == participante.getSexo()
                && a.getCarreraAplicacion() == participante.getCarrera()
                && a.getCohorteAplicacion() == participante.getCohorte()
                && a.getGrupoAcademicoAplicacion() == participante.getGrupoAcademico()
                && a.getEstado() == EstadoAsignacion.ASIGNADO));
    }

    @Test
    void startOrResumeAttemptReutilizaIntentoExistente() {
        IntentoTest existing = new IntentoTest();
        when(intentos.findByAsignacionId(99L)).thenReturn(Optional.of(existing));

        assertThat(service.startOrResumeAttempt(access(99L), "device", "127.0.0.1")).isSameAs(existing);
    }

    @Test
    void startOrResumeAttemptCreaIntentoYMarcaAsignacionEnProgreso() {
        AsignacionTest asignacion = new AsignacionTest();
        asignacion.setEstado(EstadoAsignacion.ASIGNADO);
        when(intentos.findByAsignacionId(99L)).thenReturn(Optional.empty());
        when(asignaciones.findByIdForUpdate(99L)).thenReturn(Optional.of(asignacion));

        IntentoTest intento = service.startOrResumeAttempt(access(99L), "device", "127.0.0.1");

        assertThat(intento.getAsignacion()).isSameAs(asignacion);
        assertThat(intento.getEstado()).isEqualTo(EstadoIntento.EN_PROGRESO);
        assertThat(asignacion.getEstado()).isEqualTo(EstadoAsignacion.EN_PROGRESO);
        verify(intentos).save(intento);
    }

    @Test
    void startSubtestRechazaSubtestNoHabilitadoEnSesion() {
        IntentoTest intento = intentoConSesion();
        intento.getAsignacion().setId(99L);
        when(intentos.findByIdForUpdate(5L)).thenReturn(Optional.of(intento));
        when(sesionSubtests.findBySesionAplicacionIdAndSubtestId(10L, 20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startSubtest(access(99L), 5L, 20L)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Subtest no habilitado");
    }

    @Test
    void startSubtestRechazaIntentoFueraDelContextoDeAsignacion() {
        IntentoTest intento = intentoConSesion();
        intento.getAsignacion().setId(123L);
        when(intentos.findByIdForUpdate(5L)).thenReturn(Optional.of(intento));

        assertThatThrownBy(() -> service.startSubtest(access(99L), 5L, 20L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Intento fuera de alcance");
    }

    @Test
    void finishSubtestCompletaIntentoSubtestEnProgreso() {
        IntentoSubtest intentoSubtest = new IntentoSubtest();
        intentoSubtest.setEstado(EstadoIntento.EN_PROGRESO);
        when(intentoSubtests.findByIntentoIdAndSubtestId(5L, 20L)).thenReturn(Optional.of(intentoSubtest));

        intentoSubtest.setIntento(intentoWithAssignment(99L));

        IntentoSubtest finished = service.finishSubtest(access(99L), 5L, 20L, 120);

        assertThat(finished.getEstado()).isEqualTo(EstadoIntento.COMPLETADO);
        assertThat(finished.getTiempoUsadoSegundos()).isEqualTo(120);
        verify(intentoSubtests).save(intentoSubtest);
    }

    @Test
    void revokeAssignmentAnulaAsignacionEIntentoNoCompletadoYRegistraAuditoria() {
        AsignacionTest asignacion = new AsignacionTest();
        asignacion.setId(77L);
        asignacion.setSesionAplicacion(sesion(EstadoSesionAplicacion.ABIERTA));
        asignacion.setEstado(EstadoAsignacion.EN_PROGRESO);
        IntentoTest intento = new IntentoTest();
        intento.setEstado(EstadoIntento.EN_PROGRESO);
        when(asignaciones.findByIdForUpdate(77L)).thenReturn(Optional.of(asignacion));
        when(intentos.findByAsignacionId(77L)).thenReturn(Optional.of(intento));

        service.revokeAssignment(10L, 77L);

        assertThat(asignacion.getEstado()).isEqualTo(EstadoAsignacion.ANULADO);
        assertThat(intento.getEstado()).isEqualTo(EstadoIntento.ANULADO);
        assertThat(intento.getUltimaActividadEn()).isEqualTo(LocalDateTime.ofInstant(CLOCK.instant(), ZoneOffset.UTC));
        verify(asignaciones).save(asignacion);
        verify(intentos).save(intento);
        verify(audit).recordTrusted(Mockito.argThat(event -> event.action().equals("ASIGNACION_REVOCADA")
                && event.entity().equals("asignacion_test")
                && event.entityId().equals("77")
                && event.newValuesJson().contains("\"sesionId\":10")));
    }

    @Test
    void revokeAssignmentRechazaAsignacionDeOtraSesion() {
        AsignacionTest asignacion = new AsignacionTest();
        asignacion.setId(77L);
        asignacion.setSesionAplicacion(sesion(EstadoSesionAplicacion.ABIERTA));
        asignacion.getSesionAplicacion().setId(99L);
        when(asignaciones.findByIdForUpdate(77L)).thenReturn(Optional.of(asignacion));

        assertThatThrownBy(() -> service.revokeAssignment(10L, 77L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Asignacion fuera de la sesion");
    }

    @Test
    void recordIncidenceRegistraAuditoriaConParticipanteYTextoEscapado() {
        UUID participantId = UUID.randomUUID();
        when(sesiones.existsById(10L)).thenReturn(true);

        service.recordIncidence(10L, new ParticipantRuntimeService.IncidenceCommand(participantId, "Dijo \"alto\""));

        verify(audit).recordTrusted(Mockito.argThat(event -> event.action().equals("INCIDENCIA_SESION")
                && event.entity().equals("sesion_aplicacion")
                && event.entityId().equals("10")
                && event.newValuesJson().contains(participantId.toString())
                && event.newValuesJson().contains("Dijo \\\"alto\\\"")));
    }

    @Test
    void getAssignmentsForSessionIncluyeAttemptIdCuandoExisteIntento() {
        UUID participantId = UUID.randomUUID();
        AsignacionTest asignacion = new AsignacionTest();
        asignacion.setId(77L);
        asignacion.setEstado(EstadoAsignacion.EN_PROGRESO);
        asignacion.setParticipante(participante(participantId));
        asignacion.getParticipante().setNombres("Ana");
        asignacion.getParticipante().setApellidos("Lopez");
        asignacion.getParticipante().setCodigoParticipante("P-001");
        asignacion.setTokenExpiraEn(LocalDateTime.ofInstant(CLOCK.instant(), ZoneOffset.UTC).plusHours(1));
        IntentoTest intento = new IntentoTest();
        intento.setId(88L);
        intento.setEstado(EstadoIntento.EN_PROGRESO);
        intento.setUltimaActividadEn(LocalDateTime.ofInstant(CLOCK.instant(), ZoneOffset.UTC));
        intento.setAsignacion(asignacion);
        when(asignaciones.findBySesionAplicacionIdWithParticipante(10L)).thenReturn(List.of(asignacion));
        when(intentos.findByAsignacionSesionAplicacionIdWithUltimoSubtest(10L)).thenReturn(List.of(intento));
        when(sesionSubtests.countBySesionAplicacionId(10L)).thenReturn(0L);
        when(intentoSubtests.countCompletedByIntentoIds(List.of(88L))).thenReturn(List.of());

        var result = service.getAssignmentsForSession(10L);

        assertThat(result).singleElement().satisfies(row -> {
            assertThat(row.assignmentId()).isEqualTo(77L);
            assertThat(row.attemptId()).isEqualTo(88L);
        });
        verify(intentos, never()).findByAsignacionId(77L);
        verify(sesionSubtests).countBySesionAplicacionId(10L);
    }

    private static ParticipantAccessService.ParticipantAccess access(long assignmentId) {
        return new ParticipantAccessService.ParticipantAccess(assignmentId, UUID.randomUUID());
    }

    private static IntentoTest intentoWithAssignment(long assignmentId) {
        AsignacionTest asignacion = new AsignacionTest();
        asignacion.setId(assignmentId);
        IntentoTest intento = new IntentoTest();
        intento.setAsignacion(asignacion);
        return intento;
    }

    private static SesionAplicacion sesion(EstadoSesionAplicacion estado) {
        SesionAplicacion sesion = new SesionAplicacion();
        sesion.setId(10L);
        sesion.setEstado(estado);
        return sesion;
    }

    private static Participante participante(UUID id) {
        Participante participante = new Participante();
        participante.setId(id);
        participante.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        participante.setSexo(new CatalogoSexo());
        participante.setCarrera(new Carrera());
        participante.setCohorte(new Cohorte());
        participante.setGrupoAcademico(new GrupoAcademico());
        return participante;
    }

    private static IntentoTest intentoConSesion() {
        AsignacionTest asignacion = new AsignacionTest();
        asignacion.setSesionAplicacion(sesion(EstadoSesionAplicacion.ABIERTA));
        IntentoTest intento = new IntentoTest();
        intento.setEstado(EstadoIntento.EN_PROGRESO);
        intento.setAsignacion(asignacion);
        return intento;
    }
}
