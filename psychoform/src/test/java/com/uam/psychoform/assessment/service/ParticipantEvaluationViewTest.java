package com.uam.psychoform.assessment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uam.psychoform.academic.model.Participante;
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
import com.uam.psychoform.assessment.repository.SesionSubtestRepository;
import com.uam.psychoform.instrument.model.Subtest;
import com.uam.psychoform.instrument.repository.ImagenItemRepository;
import com.uam.psychoform.instrument.repository.ImagenOpcionRepository;
import com.uam.psychoform.instrument.repository.ItemLookupRepository;
import com.uam.psychoform.instrument.repository.OpcionItemLookupRepository;
import com.uam.psychoform.security.model.EstadoGeneral;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ParticipantEvaluationViewTest {
    private final AsignacionTestRepository asignaciones = Mockito.mock(AsignacionTestRepository.class);
    private final IntentoTestRepository intentos = Mockito.mock(IntentoTestRepository.class);
    private final IntentoSubtestRepository intentoSubtests = Mockito.mock(IntentoSubtestRepository.class);
    private final SesionSubtestRepository sesionSubtests = Mockito.mock(SesionSubtestRepository.class);
    private final ItemLookupRepository items = Mockito.mock(ItemLookupRepository.class);
    private final OpcionItemLookupRepository opciones = Mockito.mock(OpcionItemLookupRepository.class);
    private final ImagenItemRepository imagenesItem = Mockito.mock(ImagenItemRepository.class);
    private final ImagenOpcionRepository imagenesOpcion = Mockito.mock(ImagenOpcionRepository.class);
    private final ParticipantEvaluationView view = new ParticipantEvaluationView(asignaciones, intentos,
            intentoSubtests, sesionSubtests, items, opciones, imagenesItem, imagenesOpcion);

    @Test
    void evaluationPayloadIncluyeIntentoMetadatosYEstadoRealDeSubtests() {
        AsignacionTest asignacion = asignacion();
        IntentoTest intento = new IntentoTest();
        intento.setId(77L);
        intento.setEstado(EstadoIntento.EN_PROGRESO);
        Subtest memoria = subtest(10L, "MEM", "Memoria");
        Subtest atencion = subtest(20L, "ATT", "Atencion");
        SesionSubtest sesionMemoria = sesionSubtest(memoria, 1);
        SesionSubtest sesionAtencion = sesionSubtest(atencion, 2);
        IntentoSubtest intentoMemoria = new IntentoSubtest();
        intentoMemoria.setSubtest(memoria);
        intentoMemoria.setEstado(EstadoIntento.COMPLETADO);

        when(asignaciones.findByIdWithSessionAndParticipant(50L)).thenReturn(Optional.of(asignacion));
        when(intentos.findByAsignacionId(50L)).thenReturn(Optional.of(intento));
        when(intentoSubtests.findByIntentoIdWithSubtest(77L)).thenReturn(List.of(intentoMemoria));
        when(sesionSubtests.findBySesionAplicacionIdWithSubtestOrderByNumeroOrdenAsc(30L))
                .thenReturn(List.of(sesionMemoria, sesionAtencion));
        when(items.findBySubtestIdInAndEstadoOrderBySubtestAndItemOrder(List.of(10L, 20L), EstadoGeneral.ACTIVO))
                .thenReturn(List.of());

        ParticipantEvaluationView.EvaluationPayload payload = view.getEvaluationPayload(50L);

        assertThat(payload.id()).isEqualTo(77L);
        assertThat(payload.assignmentId()).isEqualTo(50L);
        assertThat(payload.sessionId()).isEqualTo(30L);
        assertThat(payload.participantDisplayName()).isEqualTo("Ana Lopez");
        assertThat(payload.sessionName()).isEqualTo("Sesion UAM");
        assertThat(payload.sessionStatus()).isEqualTo("ABIERTA");
        assertThat(payload.attemptStatus()).isEqualTo("EN_PROGRESO");
        assertThat(payload.subtests()).extracting(ParticipantEvaluationView.SubtestPayload::status)
                .containsExactly("COMPLETADO", "NO_INICIADO");
        verify(items).findBySubtestIdInAndEstadoOrderBySubtestAndItemOrder(List.of(10L, 20L), EstadoGeneral.ACTIVO);
    }

    private static AsignacionTest asignacion() {
        Participante participante = new Participante();
        participante.setId(UUID.randomUUID());
        participante.setNombres("Ana");
        participante.setApellidos("Lopez");
        SesionAplicacion sesion = new SesionAplicacion();
        sesion.setId(30L);
        sesion.setNombreSesion("Sesion UAM");
        sesion.setEstado(EstadoSesionAplicacion.ABIERTA);
        AsignacionTest asignacion = new AsignacionTest();
        asignacion.setId(50L);
        asignacion.setParticipante(participante);
        asignacion.setSesionAplicacion(sesion);
        asignacion.setEstado(EstadoAsignacion.EN_PROGRESO);
        return asignacion;
    }

    private static SesionSubtest sesionSubtest(Subtest subtest, int order) {
        SesionSubtest sesionSubtest = new SesionSubtest();
        sesionSubtest.setSubtest(subtest);
        sesionSubtest.setNumeroOrden(order);
        sesionSubtest.setTiempoLimiteSegundos(120);
        sesionSubtest.setPermiteAleatorizarItems(false);
        sesionSubtest.setPermiteAleatorizarOpciones(false);
        return sesionSubtest;
    }

    private static Subtest subtest(long id, String code, String name) {
        Subtest subtest = new Subtest();
        subtest.setId(id);
        subtest.setCodigoSubtest(code);
        subtest.setNombreSubtest(name);
        subtest.setInstrucciones("Instrucciones " + name);
        return subtest;
    }
}
