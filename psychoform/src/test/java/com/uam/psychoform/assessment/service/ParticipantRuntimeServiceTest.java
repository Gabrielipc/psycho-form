package com.uam.psychoform.assessment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
    private final ParticipantRuntimeService service = new ParticipantRuntimeService(sesiones, sesionSubtests,
            asignaciones, intentos, intentoSubtests, participantes, usuarios, tokenService, currentActor, CLOCK);

    @Test
    void assignParticipantRequierePermisoSesionAplicar() throws Exception {
        Method method = ParticipantRuntimeService.class.getMethod("assignParticipant",
                ParticipantRuntimeService.AssignParticipantCommand.class);

        assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAuthority('PERM_SESION_APLICAR')");
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

        assertThat(service.startOrResumeAttempt(99L, "device", "127.0.0.1")).isSameAs(existing);
    }

    @Test
    void startOrResumeAttemptCreaIntentoYMarcaAsignacionEnProgreso() {
        AsignacionTest asignacion = new AsignacionTest();
        asignacion.setEstado(EstadoAsignacion.ASIGNADO);
        when(intentos.findByAsignacionId(99L)).thenReturn(Optional.empty());
        when(asignaciones.findByIdForUpdate(99L)).thenReturn(Optional.of(asignacion));

        IntentoTest intento = service.startOrResumeAttempt(99L, "device", "127.0.0.1");

        assertThat(intento.getAsignacion()).isSameAs(asignacion);
        assertThat(intento.getEstado()).isEqualTo(EstadoIntento.EN_PROGRESO);
        assertThat(asignacion.getEstado()).isEqualTo(EstadoAsignacion.EN_PROGRESO);
        verify(intentos).save(intento);
    }

    @Test
    void startSubtestRechazaSubtestNoHabilitadoEnSesion() {
        IntentoTest intento = intentoConSesion();
        when(intentos.findByIdForUpdate(5L)).thenReturn(Optional.of(intento));
        when(sesionSubtests.findBySesionAplicacionIdAndSubtestId(10L, 20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startSubtest(5L, 20L)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Subtest no habilitado");
    }

    @Test
    void finishSubtestCompletaIntentoSubtestEnProgreso() {
        IntentoSubtest intentoSubtest = new IntentoSubtest();
        intentoSubtest.setEstado(EstadoIntento.EN_PROGRESO);
        when(intentoSubtests.findByIntentoIdAndSubtestId(5L, 20L)).thenReturn(Optional.of(intentoSubtest));

        IntentoSubtest finished = service.finishSubtest(5L, 20L, 120);

        assertThat(finished.getEstado()).isEqualTo(EstadoIntento.COMPLETADO);
        assertThat(finished.getTiempoUsadoSegundos()).isEqualTo(120);
        verify(intentoSubtests).save(intentoSubtest);
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
