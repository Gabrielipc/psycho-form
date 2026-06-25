package com.uam.psychoform.assessment.service;

import com.uam.psychoform.assessment.model.EstadoIntento;
import com.uam.psychoform.assessment.model.IntentoTest;
import com.uam.psychoform.assessment.model.OpcionSeleccionadaRespuesta;
import com.uam.psychoform.assessment.model.OpcionSeleccionadaRespuestaId;
import com.uam.psychoform.assessment.model.RespuestaItem;
import com.uam.psychoform.assessment.repository.IntentoTestRepository;
import com.uam.psychoform.assessment.repository.OpcionSeleccionadaRespuestaRepository;
import com.uam.psychoform.assessment.repository.RespuestaItemRepository;
import com.uam.psychoform.instrument.model.Item;
import com.uam.psychoform.instrument.model.OpcionItem;
import com.uam.psychoform.instrument.repository.ItemLookupRepository;
import com.uam.psychoform.instrument.repository.OpcionItemLookupRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ParticipantAnswerService {
    private final IntentoTestRepository intentos;
    private final RespuestaItemRepository respuestas;
    private final OpcionSeleccionadaRespuestaRepository opcionesSeleccionadas;
    private final ItemLookupRepository items;
    private final OpcionItemLookupRepository opciones;

    public ParticipantAnswerService(IntentoTestRepository intentos, RespuestaItemRepository respuestas,
            OpcionSeleccionadaRespuestaRepository opcionesSeleccionadas, ItemLookupRepository items,
            OpcionItemLookupRepository opciones) {
        this.intentos = intentos;
        this.respuestas = respuestas;
        this.opcionesSeleccionadas = opcionesSeleccionadas;
        this.items = items;
        this.opciones = opciones;
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_RESPUESTA_REGISTRAR')")
    public RespuestaItem saveAnswer(SaveAnswerCommand command) {
        IntentoTest intento = intentos.findByIdForUpdate(command.intentoId())
                .orElseThrow(() -> new EntityNotFoundException("Intento no encontrado: " + command.intentoId()));
        if (intento.getEstado() == EstadoIntento.COMPLETADO || intento.getEstado() == EstadoIntento.ANULADO) {
            throw new IllegalStateException("El intento no permite respuestas");
        }
        Item item = items.findById(command.itemId())
                .orElseThrow(() -> new EntityNotFoundException("Item no encontrado: " + command.itemId()));
        List<OpcionItem> selected = opciones.findAllById(command.selectedOptionIds());
        if (selected.size() != command.selectedOptionIds().size()) {
            throw new EntityNotFoundException("Opcion no encontrada");
        }
        for (OpcionItem option : selected) {
            if (!option.getItem().getId().equals(item.getId())) {
                throw new IllegalArgumentException("La opcion no pertenece al item");
            }
        }

        RespuestaItem respuesta = respuestas.findByIntentoIdAndItemId(command.intentoId(), command.itemId())
                .orElseGet(() -> newAnswer(intento, item));
        respuesta.setRespuestaTextoAbierto(command.textAnswer());
        respuesta.setRespuestaNumerica(command.numericAnswer());
        respuesta.setTiempoUsadoSegundos(command.timeUsedSeconds());
        respuesta.setRespondidoEn(LocalDateTime.now());
        respuesta.setEsFinal(true);
        respuesta.setRequiereRevisionManual(false);
        respuestas.save(respuesta);

        if (respuesta.getId() != null) {
            opcionesSeleccionadas.deleteByRespuestaId(respuesta.getId());
        }
        for (OpcionItem option : selected) {
            OpcionSeleccionadaRespuesta selectedOption = new OpcionSeleccionadaRespuesta();
            OpcionSeleccionadaRespuestaId id = new OpcionSeleccionadaRespuestaId();
            id.setRespuestaId(respuesta.getId());
            id.setOpcionId(option.getId());
            selectedOption.setId(id);
            selectedOption.setRespuesta(respuesta);
            selectedOption.setItem(item);
            selectedOption.setOpcion(option);
            selectedOption.setSeleccionadaEn(LocalDateTime.now());
            opcionesSeleccionadas.save(selectedOption);
        }
        return respuesta;
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_RESPUESTA_REGISTRAR')")
    public List<RespuestaItem> bulkSyncAnswers(BulkSyncAnswersCommand command) {
        return command.answers().stream().map(this::saveAnswer).toList();
    }

    private static RespuestaItem newAnswer(IntentoTest intento, Item item) {
        RespuestaItem respuesta = new RespuestaItem();
        respuesta.setIntento(intento);
        respuesta.setItem(item);
        return respuesta;
    }

    public record SaveAnswerCommand(Long intentoId, Long itemId, List<Long> selectedOptionIds, String textAnswer,
            BigDecimal numericAnswer, Integer timeUsedSeconds) {
    }

    public record BulkSyncAnswersCommand(Long intentoId, List<SaveAnswerCommand> answers) {
        public BulkSyncAnswersCommand {
            answers = answers.stream()
                    .map(answer -> new SaveAnswerCommand(intentoId, answer.itemId(), answer.selectedOptionIds(),
                            answer.textAnswer(), answer.numericAnswer(), answer.timeUsedSeconds()))
                    .toList();
        }
    }
}
