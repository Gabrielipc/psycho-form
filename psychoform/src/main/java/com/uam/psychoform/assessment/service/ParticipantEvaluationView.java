package com.uam.psychoform.assessment.service;

import com.uam.psychoform.assessment.model.AsignacionTest;
import com.uam.psychoform.assessment.model.IntentoSubtest;
import com.uam.psychoform.assessment.model.IntentoTest;
import com.uam.psychoform.assessment.model.SesionSubtest;
import com.uam.psychoform.assessment.repository.AsignacionTestRepository;
import com.uam.psychoform.assessment.repository.IntentoSubtestRepository;
import com.uam.psychoform.assessment.repository.IntentoTestRepository;
import com.uam.psychoform.assessment.repository.SesionSubtestRepository;
import com.uam.psychoform.instrument.model.ImagenItem;
import com.uam.psychoform.instrument.model.ImagenOpcion;
import com.uam.psychoform.instrument.model.Item;
import com.uam.psychoform.instrument.model.OpcionItem;
import com.uam.psychoform.instrument.repository.ImagenItemRepository;
import com.uam.psychoform.instrument.repository.ImagenOpcionRepository;
import com.uam.psychoform.instrument.repository.ItemLookupRepository;
import com.uam.psychoform.instrument.repository.OpcionItemLookupRepository;
import com.uam.psychoform.security.model.EstadoGeneral;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ParticipantEvaluationView {
    private final AsignacionTestRepository asignaciones;
    private final IntentoTestRepository intentos;
    private final IntentoSubtestRepository intentoSubtests;
    private final SesionSubtestRepository sesionSubtests;
    private final ItemLookupRepository items;
    private final OpcionItemLookupRepository opciones;
    private final ImagenItemRepository imagenesItem;
    private final ImagenOpcionRepository imagenesOpcion;

    public ParticipantEvaluationView(AsignacionTestRepository asignaciones, IntentoTestRepository intentos,
            IntentoSubtestRepository intentoSubtests, SesionSubtestRepository sesionSubtests,
            ItemLookupRepository items, OpcionItemLookupRepository opciones,
            ImagenItemRepository imagenesItem, ImagenOpcionRepository imagenesOpcion) {
        this.asignaciones = asignaciones;
        this.intentos = intentos;
        this.intentoSubtests = intentoSubtests;
        this.sesionSubtests = sesionSubtests;
        this.items = items;
        this.opciones = opciones;
        this.imagenesItem = imagenesItem;
        this.imagenesOpcion = imagenesOpcion;
    }

    public EvaluationPayload getEvaluationPayload(long asignacionId) {
        AsignacionTest asignacion = asignaciones.findById(asignacionId)
                .orElseThrow(() -> new EntityNotFoundException("Asignacion no encontrada: " + asignacionId));
        Optional<IntentoTest> intento = intentos.findByAsignacionId(asignacionId);
        Map<Long, String> subtestStatuses = intento
                .map(existing -> intentoSubtests.findByIntentoId(existing.getId()).stream()
                        .collect(Collectors.toMap(status -> status.getSubtest().getId(),
                                status -> status.getEstado().name(), (first, second) -> first)))
                .orElseGet(Map::of);
        List<SubtestPayload> subtests = sesionSubtests
                .findBySesionAplicacionIdOrderByNumeroOrdenAsc(asignacion.getSesionAplicacion().getId()).stream()
                .map(sesionSubtest -> toSubtestPayload(sesionSubtest, subtestStatuses))
                .toList();
        Long attemptId = intento.map(IntentoTest::getId).orElse(null);
        String attemptStatus = intento.map(existing -> existing.getEstado().name()).orElse("NO_INICIADO");
        return new EvaluationPayload(attemptId, asignacion.getId(), asignacion.getSesionAplicacion().getId(),
                participantDisplayName(asignacion), asignacion.getSesionAplicacion().getNombreSesion(),
                asignacion.getSesionAplicacion().getEstado().name(), attemptStatus, subtests);
    }

    public SubtestPayload getScopedSubtestPayload(ParticipantAccessService.ParticipantAccess access, long attemptId,
            long subtestId) {
        IntentoTest intento = intentos.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Intento no encontrado: " + attemptId));
        if (!Objects.equals(intento.getAsignacion().getId(), access.assignmentId())) {
            throw new AccessDeniedException("Intento fuera de alcance");
        }
        return getEvaluationPayload(access.assignmentId()).subtests().stream()
                .filter(subtest -> subtest.subtestId() == subtestId)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Subtest no encontrado en intento"));
    }

    private SubtestPayload toSubtestPayload(SesionSubtest sesionSubtest, Map<Long, String> subtestStatuses) {
        List<ItemPayload> itemPayloads = items
                .findBySubtestIdAndEstadoOrderByNumeroOrdenAsc(sesionSubtest.getSubtest().getId(), EstadoGeneral.ACTIVO)
                .stream()
                .map(this::toItemPayload)
                .toList();
        return new SubtestPayload(sesionSubtest.getSubtest().getId(), sesionSubtest.getSubtest().getCodigoSubtest(),
                sesionSubtest.getSubtest().getNombreSubtest(), sesionSubtest.getSubtest().getInstrucciones(),
                sesionSubtest.getNumeroOrden(), sesionSubtest.getTiempoLimiteSegundos(),
                sesionSubtest.getPermiteAleatorizarItems(), sesionSubtest.getPermiteAleatorizarOpciones(),
                subtestStatuses.getOrDefault(sesionSubtest.getSubtest().getId(), "NO_INICIADO"), itemPayloads);
    }

    private ItemPayload toItemPayload(Item item) {
        List<OptionPayload> optionPayloads = opciones
                .findByItemIdAndEstadoOrderByNumeroOrdenAsc(item.getId(), EstadoGeneral.ACTIVO).stream()
                .map(this::toOptionPayload)
                .toList();
        List<ItemImagePayload> imagePayloads = imagenesItem
                .findByItemIdOrderByNumeroOrdenAsc(item.getId()).stream()
                .map(this::toItemImagePayload)
                .toList();
        return new ItemPayload(item.getId(), item.getCodigoItem(), item.getTipoItem().name(),
                item.getTipoRespuesta().name(), item.getEnunciado(), item.getInstruccion(), item.getNumeroOrden(),
                item.getTiempoLimiteSegundos(), item.getEsObligatorio(), imagePayloads, optionPayloads);
    }

    private OptionPayload toOptionPayload(OpcionItem opcion) {
        List<OptionImagePayload> imagePayloads = imagenesOpcion
                .findByOpcionIdOrderByNumeroOrdenAsc(opcion.getId()).stream()
                .map(this::toOptionImagePayload)
                .toList();
        return new OptionPayload(opcion.getId(), opcion.getCodigoOpcion(), opcion.getTextoOpcion(),
                opcion.getNumeroOrden(), opcion.getValorOrdinal(), imagePayloads);
    }

    private ItemImagePayload toItemImagePayload(ImagenItem image) {
        return new ItemImagePayload(
                image.getId(),
                "/items/images/resources/" + image.getRecurso().getId(),
                image.getRolImagen(),
                image.getNumeroOrden(),
                image.getTextoAlternativo()
        );
    }

    private OptionImagePayload toOptionImagePayload(ImagenOpcion image) {
        return new OptionImagePayload(
                image.getId(),
                "/items/images/resources/" + image.getRecurso().getId(),
                image.getNumeroOrden(),
                image.getTextoAlternativo()
        );
    }

    private static String participantDisplayName(AsignacionTest asignacion) {
        return (asignacion.getParticipante().getNombres() + " " + asignacion.getParticipante().getApellidos()).trim();
    }

    public record EvaluationPayload(Long id, long assignmentId, long sessionId, String participantDisplayName,
            String sessionName, String sessionStatus, String attemptStatus, List<SubtestPayload> subtests) {
    }

    public record SubtestPayload(long subtestId, String code, String name, String instructions, int order,
            Integer timeLimitSeconds, boolean randomizeItems, boolean randomizeOptions, String status,
            List<ItemPayload> items) {
    }

    public record ItemPayload(long itemId, String code, String itemType, String responseType, String prompt,
            String instruction, int order, Integer timeLimitSeconds, boolean required,
            List<ItemImagePayload> images, List<OptionPayload> options) {
    }

    public record OptionPayload(long optionId, String code, String text, int order, java.math.BigDecimal ordinalValue,
            List<OptionImagePayload> images) {
    }

    public record ItemImagePayload(long imageId, String url, String role, int order, String altText) {
    }

    public record OptionImagePayload(long imageId, String url, int order, String altText) {
    }
}
