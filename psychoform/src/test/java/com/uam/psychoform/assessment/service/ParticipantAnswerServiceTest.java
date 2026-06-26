package com.uam.psychoform.assessment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uam.psychoform.assessment.model.EstadoIntento;
import com.uam.psychoform.assessment.model.IntentoTest;
import com.uam.psychoform.assessment.model.RespuestaItem;
import com.uam.psychoform.assessment.repository.IntentoTestRepository;
import com.uam.psychoform.assessment.repository.OpcionSeleccionadaRespuestaRepository;
import com.uam.psychoform.assessment.repository.RespuestaItemRepository;
import com.uam.psychoform.instrument.model.Item;
import com.uam.psychoform.instrument.model.OpcionItem;
import com.uam.psychoform.instrument.repository.ItemLookupRepository;
import com.uam.psychoform.instrument.repository.OpcionItemLookupRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;

class ParticipantAnswerServiceTest {
    private final IntentoTestRepository intentos = Mockito.mock(IntentoTestRepository.class);
    private final RespuestaItemRepository respuestas = Mockito.mock(RespuestaItemRepository.class);
    private final OpcionSeleccionadaRespuestaRepository opcionesSeleccionadas = Mockito.mock(OpcionSeleccionadaRespuestaRepository.class);
    private final ItemLookupRepository items = Mockito.mock(ItemLookupRepository.class);
    private final OpcionItemLookupRepository opciones = Mockito.mock(OpcionItemLookupRepository.class);
    private final ParticipantAnswerService service = new ParticipantAnswerService(intentos, respuestas,
            opcionesSeleccionadas, items, opciones);

    @Test
    void operacionesDeRespuestaDeParticipanteNoUsanPermisoInternoRespuestaRegistrar() throws Exception {
        assertThat(ParticipantAnswerService.class.getMethod("saveAnswer",
                ParticipantAccessService.ParticipantAccess.class, ParticipantAnswerService.SaveAnswerCommand.class)
                .getAnnotation(PreAuthorize.class)).isNull();
        assertThat(ParticipantAnswerService.class.getMethod("bulkSyncAnswers",
                ParticipantAccessService.ParticipantAccess.class, ParticipantAnswerService.BulkSyncAnswersCommand.class)
                .getAnnotation(PreAuthorize.class)).isNull();
    }

    @Test
    void saveAnswerEsIdempotentePorIntentoEItemYReemplazaOpciones() {
        IntentoTest intento = intento(EstadoIntento.EN_PROGRESO);
        Item item = item(10L);
        OpcionItem opcion = opcion(20L, item);
        RespuestaItem existente = new RespuestaItem();
        existente.setId(30L);
        when(intentos.findByIdForUpdate(1L)).thenReturn(Optional.of(intento));
        when(items.findById(10L)).thenReturn(Optional.of(item));
        when(opciones.findAllById(List.of(20L))).thenReturn(List.of(opcion));
        when(respuestas.findByIntentoIdAndItemId(1L, 10L)).thenReturn(Optional.of(existente));

        intento.setAsignacion(asignacion(99L));

        RespuestaItem saved = service.saveAnswer(access(99L), new ParticipantAnswerService.SaveAnswerCommand(1L, 10L,
                List.of(20L), "texto", BigDecimal.TEN, 45));

        assertThat(saved).isSameAs(existente);
        assertThat(saved.getRespuestaTextoAbierto()).isEqualTo("texto");
        assertThat(saved.getRespuestaNumerica()).isEqualByComparingTo(BigDecimal.TEN);
        verify(opcionesSeleccionadas).deleteByRespuestaId(30L);
        verify(respuestas).save(existente);
    }

    @Test
    void saveAnswerRechazaOpcionDeOtroItem() {
        IntentoTest intento = intento(EstadoIntento.EN_PROGRESO);
        Item item = item(10L);
        OpcionItem opcion = opcion(20L, item(99L));
        when(intentos.findByIdForUpdate(1L)).thenReturn(Optional.of(intento));
        when(items.findById(10L)).thenReturn(Optional.of(item));
        when(opciones.findAllById(List.of(20L))).thenReturn(List.of(opcion));

        intento.setAsignacion(asignacion(99L));

        assertThatThrownBy(() -> service.saveAnswer(access(99L), new ParticipantAnswerService.SaveAnswerCommand(1L, 10L,
                List.of(20L), null, null, null))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pertenece al item");
    }

    @Test
    void saveAnswerRechazaIntentoCompletado() {
        IntentoTest intento = intento(EstadoIntento.COMPLETADO);
        intento.setAsignacion(asignacion(99L));
        when(intentos.findByIdForUpdate(1L)).thenReturn(Optional.of(intento));

        assertThatThrownBy(() -> service.saveAnswer(access(99L), new ParticipantAnswerService.SaveAnswerCommand(1L, 10L,
                List.of(), null, null, null))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intento no permite respuestas");
    }

    @Test
    void saveAnswerRechazaIntentoFueraDelContextoDeAsignacion() {
        IntentoTest intento = intento(EstadoIntento.EN_PROGRESO);
        intento.setAsignacion(asignacion(123L));
        when(intentos.findByIdForUpdate(1L)).thenReturn(Optional.of(intento));

        assertThatThrownBy(() -> service.saveAnswer(access(99L), new ParticipantAnswerService.SaveAnswerCommand(1L,
                10L, List.of(), null, null, null))).isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Intento fuera de alcance");
    }

    private static IntentoTest intento(EstadoIntento estado) {
        IntentoTest intento = new IntentoTest();
        intento.setEstado(estado);
        return intento;
    }

    private static com.uam.psychoform.assessment.model.AsignacionTest asignacion(Long id) {
        com.uam.psychoform.assessment.model.AsignacionTest asignacion =
                new com.uam.psychoform.assessment.model.AsignacionTest();
        asignacion.setId(id);
        return asignacion;
    }

    private static ParticipantAccessService.ParticipantAccess access(long assignmentId) {
        return new ParticipantAccessService.ParticipantAccess(assignmentId, java.util.UUID.randomUUID());
    }

    private static Item item(Long id) {
        Item item = new Item();
        item.setId(id);
        return item;
    }

    private static OpcionItem opcion(Long id, Item item) {
        OpcionItem opcion = new OpcionItem();
        opcion.setId(id);
        opcion.setItem(item);
        return opcion;
    }
}
