package com.uam.psychoform.assessment.service;

import com.uam.psychoform.assessment.entity.AsignacionTest;
import com.uam.psychoform.assessment.entity.SesionSubtest;
import com.uam.psychoform.assessment.repository.AsignacionTestRepository;
import com.uam.psychoform.assessment.repository.SesionSubtestRepository;
import com.uam.psychoform.instrument.entity.Item;
import com.uam.psychoform.instrument.entity.OpcionItem;
import com.uam.psychoform.instrument.repository.ItemLookupRepository;
import com.uam.psychoform.instrument.repository.OpcionItemLookupRepository;
import com.uam.psychoform.security.entity.EstadoGeneral;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ParticipantEvaluationView {
    private final AsignacionTestRepository asignaciones;
    private final SesionSubtestRepository sesionSubtests;
    private final ItemLookupRepository items;
    private final OpcionItemLookupRepository opciones;

    public ParticipantEvaluationView(AsignacionTestRepository asignaciones, SesionSubtestRepository sesionSubtests,
            ItemLookupRepository items, OpcionItemLookupRepository opciones) {
        this.asignaciones = asignaciones;
        this.sesionSubtests = sesionSubtests;
        this.items = items;
        this.opciones = opciones;
    }

    public EvaluationPayload getEvaluationPayload(long asignacionId) {
        AsignacionTest asignacion = asignaciones.findById(asignacionId)
                .orElseThrow(() -> new EntityNotFoundException("Asignacion no encontrada: " + asignacionId));
        List<SubtestPayload> subtests = sesionSubtests
                .findBySesionAplicacionIdOrderByNumeroOrdenAsc(asignacion.getSesionAplicacion().getId()).stream()
                .map(this::toSubtestPayload)
                .toList();
        return new EvaluationPayload(asignacion.getId(), asignacion.getSesionAplicacion().getId(), subtests);
    }

    private SubtestPayload toSubtestPayload(SesionSubtest sesionSubtest) {
        List<ItemPayload> itemPayloads = items
                .findBySubtestIdAndEstadoOrderByNumeroOrdenAsc(sesionSubtest.getSubtest().getId(), EstadoGeneral.ACTIVO)
                .stream()
                .map(this::toItemPayload)
                .toList();
        return new SubtestPayload(sesionSubtest.getSubtest().getId(), sesionSubtest.getSubtest().getCodigoSubtest(),
                sesionSubtest.getSubtest().getNombreSubtest(), sesionSubtest.getSubtest().getInstrucciones(),
                sesionSubtest.getNumeroOrden(), sesionSubtest.getTiempoLimiteSegundos(),
                sesionSubtest.getPermiteAleatorizarItems(), sesionSubtest.getPermiteAleatorizarOpciones(),
                itemPayloads);
    }

    private ItemPayload toItemPayload(Item item) {
        List<OptionPayload> optionPayloads = opciones
                .findByItemIdAndEstadoOrderByNumeroOrdenAsc(item.getId(), EstadoGeneral.ACTIVO).stream()
                .map(this::toOptionPayload)
                .toList();
        return new ItemPayload(item.getId(), item.getCodigoItem(), item.getTipoItem().name(),
                item.getTipoRespuesta().name(), item.getEnunciado(), item.getInstruccion(), item.getNumeroOrden(),
                item.getTiempoLimiteSegundos(), item.getEsObligatorio(), optionPayloads);
    }

    private OptionPayload toOptionPayload(OpcionItem opcion) {
        return new OptionPayload(opcion.getId(), opcion.getCodigoOpcion(), opcion.getTextoOpcion(),
                opcion.getNumeroOrden(), opcion.getValorOrdinal());
    }

    public record EvaluationPayload(long assignmentId, long sessionId, List<SubtestPayload> subtests) {
    }

    public record SubtestPayload(long subtestId, String code, String name, String instructions, int order,
            Integer timeLimitSeconds, boolean randomizeItems, boolean randomizeOptions, List<ItemPayload> items) {
    }

    public record ItemPayload(long itemId, String code, String itemType, String responseType, String prompt,
            String instruction, int order, Integer timeLimitSeconds, boolean required, List<OptionPayload> options) {
    }

    public record OptionPayload(long optionId, String code, String text, int order, java.math.BigDecimal ordinalValue) {
    }
}
