package com.uam.psychoform.reporting.service;

import com.uam.psychoform.assessment.model.EstadoAsignacion;
import com.uam.psychoform.assessment.model.EstadoIntento;
import com.uam.psychoform.assessment.model.IntentoTest;
import com.uam.psychoform.assessment.model.RespuestaItem;
import com.uam.psychoform.assessment.model.OpcionSeleccionadaRespuesta;
import com.uam.psychoform.assessment.model.SesionSubtest;
import com.uam.psychoform.assessment.repository.AsignacionTestRepository;
import com.uam.psychoform.assessment.repository.IntentoTestRepository;
import com.uam.psychoform.assessment.repository.SesionSubtestRepository;
import com.uam.psychoform.assessment.repository.RespuestaItemRepository;
import com.uam.psychoform.assessment.repository.OpcionSeleccionadaRespuestaRepository;
import com.uam.psychoform.instrument.model.Item;
import com.uam.psychoform.instrument.model.OpcionItem;
import com.uam.psychoform.instrument.model.ClaveRespuesta;
import com.uam.psychoform.instrument.model.ImagenItem;
import com.uam.psychoform.instrument.model.ImagenOpcion;
import com.uam.psychoform.instrument.model.Baremo;
import com.uam.psychoform.instrument.model.RangoBaremo;
import com.uam.psychoform.instrument.repository.BaremoRepository;
import com.uam.psychoform.instrument.repository.ItemLookupRepository;
import com.uam.psychoform.instrument.repository.OpcionItemLookupRepository;
import com.uam.psychoform.instrument.repository.ClaveRespuestaLookupRepository;
import com.uam.psychoform.instrument.repository.ImagenItemRepository;
import com.uam.psychoform.instrument.repository.ImagenOpcionRepository;
import com.uam.psychoform.instrument.repository.RangoBaremoRepository;
import com.uam.psychoform.scoring.model.Resultado;
import com.uam.psychoform.scoring.model.ResultadoDimension;
import com.uam.psychoform.scoring.model.CalificacionRespuesta;
import com.uam.psychoform.scoring.repository.ResultadoDimensionRepository;
import com.uam.psychoform.scoring.repository.ResultadoRepository;
import com.uam.psychoform.scoring.repository.CalificacionRespuestaRepository;
import com.uam.psychoform.security.SecurityPermissions;
import com.uam.psychoform.security.model.EstadoGeneral;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ResultQueryService {
    private static final String DISCLAIMER = "El sistema no emite diagnosticos psicologicos definitivos.";
    private static final int MIN_GROUP_SIZE_FOR_BREAKDOWN = 5;

    private final AsignacionTestRepository asignaciones;
    private final IntentoTestRepository intentos;
    private final ResultadoRepository resultados;
    private final ResultadoDimensionRepository dimensiones;
    private final SesionSubtestRepository sesionSubtests;
    private final ItemLookupRepository items;
    private final OpcionItemLookupRepository opciones;
    private final ClaveRespuestaLookupRepository claves;
    private final BaremoRepository baremos;
    private final RangoBaremoRepository rangosBaremo;
    private final RespuestaItemRepository respuestas;
    private final OpcionSeleccionadaRespuestaRepository opcionesSeleccionadas;
    private final CalificacionRespuestaRepository calificaciones;
    private final ImagenItemRepository imagenesItem;
    private final ImagenOpcionRepository imagenesOpcion;

    public ResultQueryService(AsignacionTestRepository asignaciones, IntentoTestRepository intentos,
            ResultadoRepository resultados, ResultadoDimensionRepository dimensiones,
            SesionSubtestRepository sesionSubtests, ItemLookupRepository items,
            OpcionItemLookupRepository opciones, ClaveRespuestaLookupRepository claves,
            BaremoRepository baremos, RangoBaremoRepository rangosBaremo,
            RespuestaItemRepository respuestas, OpcionSeleccionadaRespuestaRepository opcionesSeleccionadas,
            CalificacionRespuestaRepository calificaciones, ImagenItemRepository imagenesItem,
            ImagenOpcionRepository imagenesOpcion) {
        this.asignaciones = asignaciones;
        this.intentos = intentos;
        this.resultados = resultados;
        this.dimensiones = dimensiones;
        this.sesionSubtests = sesionSubtests;
        this.items = items;
        this.opciones = opciones;
        this.claves = claves;
        this.baremos = baremos;
        this.rangosBaremo = rangosBaremo;
        this.respuestas = respuestas;
        this.opcionesSeleccionadas = opcionesSeleccionadas;
        this.calificaciones = calificaciones;
        this.imagenesItem = imagenesItem;
        this.imagenesOpcion = imagenesOpcion;
    }

    @PreAuthorize(SecurityPermissions.RESULTADO_VER)
    public IndividualResultView getAttemptResult(long attemptId) {
        Resultado resultado = resultados.findByIntentoId(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Resultado no encontrado para intento: " + attemptId));
        List<DimensionResultView> dimensionViews = dimensiones.findByResultadoId(resultado.getId()).stream()
                .map(d -> new DimensionResultView(d.getDimensionResultado().getId(),
                        d.getDimensionResultado().getNombreDimension(), d.getPuntajeDirecto(), d.getCategoria(),
                        d.getPercentil(), d.getInterpretacion()))
                .toList();
        Long versionId = resultado.getIntento().getAsignacion().getSesionAplicacion().getVersionTest().getId();
        TotalResultInterpretationView totalInterpretation = totalInterpretation(versionId, resultado.getPuntajeTotalDirecto());
        return new IndividualResultView(attemptId, resultado.getId(), resultado.getEstado(),
                resultado.getPuntajeTotalDirecto(), totalInterpretation, dimensionViews, DISCLAIMER);
    }

    @PreAuthorize(SecurityPermissions.RESULTADO_VER)
    public DetailedAttemptResultView getDetailedAttemptResult(long attemptId) {
        IntentoTest intento = intentos.findByIdWithAsignacionAndParticipanteAndSesion(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Intento no encontrado: " + attemptId));

        var asignacion = intento.getAsignacion();
        var participante = asignacion.getParticipante();
        var sesion = asignacion.getSesionAplicacion();
        var version = sesion.getVersionTest();
        var test = version.getTest();

        String carrera = asignacion.getCarreraAplicacion() != null
                ? asignacion.getCarreraAplicacion().getNombreCarrera()
                : (participante.getCarrera() != null ? participante.getCarrera().getNombreCarrera() : null);

        String grupoAcademico = asignacion.getGrupoAcademicoAplicacion() != null
                ? asignacion.getGrupoAcademicoAplicacion().getNombreGrupo()
                : (participante.getGrupoAcademico() != null ? participante.getGrupoAcademico().getNombreGrupo() : null);

        String cohorte = asignacion.getCohorteAplicacion() != null
                ? asignacion.getCohorteAplicacion().getNombreCohorte()
                : (participante.getCohorte() != null ? participante.getCohorte().getNombreCohorte() : null);

        String participantName = (participante.getNombres() + " " + participante.getApellidos()).trim();

        Optional<Resultado> resultadoOpt = resultados.findByIntentoId(attemptId);
        Long resultId = resultadoOpt.map(Resultado::getId).orElse(null);
        BigDecimal totalScore = resultadoOpt.map(Resultado::getPuntajeTotalDirecto).orElse(null);
        LocalDateTime scoredAt = resultadoOpt.map(Resultado::getCalculadoEn).orElse(null);
        Integer correctCount = resultadoOpt.map(Resultado::getCantidadCorrectas).orElse(null);
        Integer incorrectCount = resultadoOpt.map(Resultado::getCantidadIncorrectas).orElse(null);
        Integer pendingReviewCount = resultadoOpt.map(Resultado::getCantidadPendientesRevision).orElse(null);
        Boolean requiresManualReview = resultadoOpt.map(Resultado::getRequiereRevisionManual).orElse(null);

        List<DimensionResultView> dimensionViews = List.of();
        TotalResultInterpretationView totalInterpretation = null;
        if (resultId != null) {
            dimensionViews = dimensiones.findByResultadoId(resultId).stream()
                    .map(d -> new DimensionResultView(d.getDimensionResultado().getId(),
                            d.getDimensionResultado().getNombreDimension(), d.getPuntajeDirecto(), d.getCategoria(),
                            d.getPercentil(), d.getInterpretacion()))
                    .toList();
            totalInterpretation = totalInterpretation(version.getId(), totalScore);
        }

        Map<Long, CalificacionRespuesta> qualificationsByRespuestaId = Map.of();
        if (resultId != null) {
            qualificationsByRespuestaId = calificaciones.findByResultadoId(resultId).stream()
                    .collect(Collectors.toMap(c -> c.getRespuesta().getId(), Function.identity(), (f, s) -> f));
        }

        List<SesionSubtest> sessionSubtests = sesionSubtests
                .findBySesionAplicacionIdWithSubtestOrderByNumeroOrdenAsc(sesion.getId());
        List<Long> subtestIds = sessionSubtests.stream().map(ss -> ss.getSubtest().getId()).toList();

        List<Item> allItems = subtestIds.isEmpty() ? List.of()
                : items.findBySubtestIdInAndEstadoOrderBySubtestAndItemOrder(subtestIds, EstadoGeneral.ACTIVO);
        List<Long> itemIds = allItems.stream().map(Item::getId).toList();
        Map<Long, List<Item>> itemsBySubtestId = allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getSubtest().getId()));

        List<OpcionItem> allOptions = itemIds.isEmpty() ? List.of()
                : opciones.findByItemIdInAndEstadoOrderByItemAndOptionOrder(itemIds, EstadoGeneral.ACTIVO);
        List<Long> optionIds = allOptions.stream().map(OpcionItem::getId).toList();
        Map<Long, List<OpcionItem>> optionsByItemId = allOptions.stream()
                .collect(Collectors.groupingBy(option -> option.getItem().getId()));

        Map<Long, ClaveRespuesta> keysByItemId = claves.findOfficialKeysByVersionId(version.getId()).stream()
                .collect(Collectors.toMap(key -> key.getItem().getId(), Function.identity(), (f, s) -> f));

        List<RespuestaItem> participantResponses = respuestas.findByIntentoIdWithItem(attemptId);
        Map<Long, RespuestaItem> responseByItemId = participantResponses.stream()
                .collect(Collectors.toMap(resp -> resp.getItem().getId(), Function.identity(), (f, s) -> f));
        List<Long> responseIds = participantResponses.stream().map(RespuestaItem::getId).toList();

        Map<Long, List<OpcionSeleccionadaRespuesta>> selectionsByResponseId = responseIds.isEmpty() ? Map.of()
                : opcionesSeleccionadas.findByRespuestaIdIn(responseIds).stream()
                        .collect(Collectors.groupingBy(sel -> sel.getRespuesta().getId()));

        Map<Long, List<ImagenItem>> imagesByItemId = itemIds.isEmpty() ? Map.of()
                : imagenesItem.findByItemIdInOrderByItemAndImageOrder(itemIds).stream()
                        .collect(Collectors.groupingBy(img -> img.getItem().getId()));

        Map<Long, List<ImagenOpcion>> imagesByOptionId = optionIds.isEmpty() ? Map.of()
                : imagenesOpcion.findByOpcionIdInOrderByOptionAndImageOrder(optionIds).stream()
                        .collect(Collectors.groupingBy(img -> img.getOpcion().getId()));

        Map<Long, CalificacionRespuesta> finalQualifications = qualificationsByRespuestaId;
        Map<Long, List<OpcionSeleccionadaRespuesta>> finalSelections = selectionsByResponseId;

        List<SubtestDetailedResponseView> subtestViews = sessionSubtests.stream().map(ss -> {
            var subtest = ss.getSubtest();
            List<Item> subtestItems = itemsBySubtestId.getOrDefault(subtest.getId(), List.of());

            List<ItemDetailedResponseView> itemViews = subtestItems.stream().map(item -> {
                RespuestaItem resp = responseByItemId.get(item.getId());
                ResponseDetailView respDetail = null;
                List<Long> selectedOptionIds = List.of();

                if (resp != null) {
                    CalificacionRespuesta cal = finalQualifications.get(resp.getId());
                    selectedOptionIds = finalSelections.getOrDefault(resp.getId(), List.of()).stream()
                            .map(sel -> sel.getOpcion().getId())
                            .toList();

                    respDetail = new ResponseDetailView(
                            resp.getId(),
                            resp.getRespuestaTextoAbierto(),
                            resp.getRespuestaNumerica(),
                            resp.getTiempoUsadoSegundos(),
                            resp.getRespondidoEn(),
                            cal != null,
                            cal != null ? cal.getPuntajeObtenido() : null,
                            cal != null ? cal.getEsCorrecta() : null,
                            resp.getRequiereRevisionManual(),
                            cal != null ? cal.getObservacion() : null
                    );
                }

                List<OpcionItem> itemOptions = optionsByItemId.getOrDefault(item.getId(), List.of());
                ClaveRespuesta key = keysByItemId.get(item.getId());
                Long correctOptionId = (key != null && key.getOpcionCorrecta() != null) ? key.getOpcionCorrecta().getId() : null;
                List<Long> finalSelectedOptionIds = selectedOptionIds;

                List<OptionDetailedResponseView> optionViews = itemOptions.stream().map(opt -> {
                    List<OptionImagePayload> optImages = imagesByOptionId.getOrDefault(opt.getId(), List.of()).stream()
                            .map(img -> new OptionImagePayload(img.getId(), "/items/images/resources/" + img.getRecurso().getId(), img.getNumeroOrden(), img.getTextoAlternativo()))
                            .toList();
                    boolean isCorrect = correctOptionId != null && opt.getId().equals(correctOptionId);
                    boolean isSelected = finalSelectedOptionIds.contains(opt.getId());

                    return new OptionDetailedResponseView(
                            opt.getId(),
                            opt.getCodigoOpcion(),
                            opt.getTextoOpcion(),
                            opt.getNumeroOrden(),
                            opt.getValorOrdinal(),
                            optImages,
                            isCorrect,
                            isSelected
                    );
                }).toList();

                List<ItemImagePayload> itemImages = imagesByItemId.getOrDefault(item.getId(), List.of()).stream()
                        .map(img -> new ItemImagePayload(img.getId(), "/items/images/resources/" + img.getRecurso().getId(), img.getRolImagen(), img.getNumeroOrden(), img.getTextoAlternativo()))
                        .toList();

                return new ItemDetailedResponseView(
                        item.getId(),
                        item.getCodigoItem(),
                        item.getNumeroOrden(),
                        item.getEnunciado(),
                        item.getTipoItem().name(),
                        item.getTipoRespuesta().name(),
                        item.getPuntajeBase(),
                        item.getTiempoLimiteSegundos(),
                        item.getEsObligatorio(),
                        itemImages,
                        optionViews,
                        respDetail
                );
            }).toList();

            return new SubtestDetailedResponseView(
                    subtest.getId(),
                    subtest.getNombreSubtest(),
                    subtest.getCodigoSubtest(),
                    ss.getNumeroOrden(),
                    itemViews
            );
        }).toList();

        return new DetailedAttemptResultView(
                attemptId,
                resultId,
                test.getNombreTest(),
                version.getNumeroVersion(),
                participantName,
                participante.getCodigoParticipante(),
                grupoAcademico,
                carrera,
                cohorte,
                intento.getEstado().name(),
                intento.getIniciadoEn(),
                intento.getFinalizadoEn(),
                intento.getTiempoTotalSegundos(),
                intento.getInformacionDispositivo(),
                intento.getDireccionIp(),
                totalScore,
                scoredAt,
                correctCount,
                incorrectCount,
                pendingReviewCount,
                requiresManualReview,
                totalInterpretation,
                dimensionViews,
                subtestViews,
                DISCLAIMER
        );
    }

    private TotalResultInterpretationView totalInterpretation(Long versionId, BigDecimal totalScore) {
        if (versionId == null || totalScore == null) {
            return null;
        }
        Optional<Baremo> baremo = baremos.findPreferredTotalBaremo(versionId);
        if (baremo.isEmpty()) {
            return null;
        }
        Optional<RangoBaremo> range = rangosBaremo.findMatchingRange(baremo.get().getId(), totalScore);
        return range.map(r -> new TotalResultInterpretationView(
                baremo.get().getId(),
                baremo.get().getCodigoBaremo(),
                r.getId(),
                r.getCategoria(),
                r.getPercentil(),
                r.getInterpretacion(),
                r.getRecomendacion()
        )).orElse(null);
    }

    @PreAuthorize(SecurityPermissions.RESULTADO_AGREGADO_VER)
    public SessionResultSummary getSessionSummary(long sessionId) {
        long assigned = asignaciones.findBySesionAplicacionId(sessionId).size();
        List<Resultado> sessionResults = resultados.findBySessionId(sessionId);
        long scored = sessionResults.size();
        long started = intentos.findByAsignacionSesionAplicacionId(sessionId).stream()
                .filter(i -> i.getEstado() != EstadoIntento.NO_INICIADO)
                .count();
        long completedAttempts = intentos.findByAsignacionSesionAplicacionId(sessionId).stream()
                .filter(i -> i.getEstado() == EstadoIntento.COMPLETADO)
                .count();
        long completedAssignments = asignaciones.findBySesionAplicacionId(sessionId).stream()
                .filter(a -> a.getEstado() == EstadoAsignacion.COMPLETADO)
                .count();
        return new SessionResultSummary(sessionId, assigned, started,
                Math.max(completedAttempts, completedAssignments), scored);
    }

    @PreAuthorize(SecurityPermissions.RESULTADO_AGREGADO_VER)
    public List<DimensionAggregateView> getDimensionAverages(ResultFilter filter) {
        List<Resultado> sessionResults = resultados.findBySessionId(filter.sessionId());
        Collection<Long> resultIds = sessionResults.stream().map(Resultado::getId).toList();
        if (resultIds.isEmpty()) {
            return List.of();
        }
        Map<Long, List<ResultadoDimension>> byDimension = dimensiones.findByResultadoIdIn(resultIds).stream()
                .collect(Collectors.groupingBy(d -> d.getDimensionResultado().getId()));
        return byDimension.entrySet().stream()
                .map(entry -> toAggregate(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static DimensionAggregateView toAggregate(Long dimensionId, List<ResultadoDimension> rows) {
        String name = rows.getFirst().getDimensionResultado().getNombreDimension();
        if (rows.size() < MIN_GROUP_SIZE_FOR_BREAKDOWN) {
            return new DimensionAggregateView(dimensionId, name, rows.size(), null, true);
        }
        BigDecimal average = rows.stream()
                .map(ResultadoDimension::getPuntajeDirecto)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(rows.size()), 2, RoundingMode.HALF_UP);
        return new DimensionAggregateView(dimensionId, name, rows.size(), average, false);
    }

    public record IndividualResultView(long attemptId, long resultId, String status, BigDecimal totalScore,
            TotalResultInterpretationView totalInterpretation, List<DimensionResultView> dimensions,
            String disclaimer) {
    }

    public record TotalResultInterpretationView(long baremoId, String baremoCode, long rangeId, String category,
            BigDecimal percentile, String interpretation, String recommendation) {
    }

    public record DimensionResultView(long dimensionId, String name, BigDecimal directScore, String category,
            BigDecimal percentile, String interpretation) {
    }

    public record SessionResultSummary(long sessionId, long assignedCount, long startedCount, long completedCount,
            long scoredCount) {
    }

    public record ResultFilter(long sessionId) {
    }

    public record DimensionAggregateView(long dimensionId, String name, int count, BigDecimal averageDirectScore,
            boolean suppressedByPrivacyThreshold) {
    }

    public record DetailedAttemptResultView(
            long attemptId,
            Long resultId,
            String testName,
            String testVersion,
            String participantName,
            String participantCode,
            String academicGroup,
            String carrera,
            String cohorte,
            String attemptStatus,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            Integer totalTimeSeconds,
            String deviceInfo,
            String ipAddress,
            BigDecimal totalScore,
            LocalDateTime scoredAt,
            Integer correctCount,
            Integer incorrectCount,
            Integer pendingReviewCount,
            Boolean requiresManualReview,
            TotalResultInterpretationView totalInterpretation,
            List<DimensionResultView> dimensions,
            List<SubtestDetailedResponseView> subtests,
            String disclaimer
    ) {}

    public record SubtestDetailedResponseView(
            long subtestId,
            String name,
            String code,
            int order,
            List<ItemDetailedResponseView> items
    ) {}

    public record ItemDetailedResponseView(
            long itemId,
            String code,
            int order,
            String prompt,
            String itemType,
            String responseType,
            BigDecimal baseScore,
            Integer timeLimitSeconds,
            boolean required,
            List<ItemImagePayload> images,
            List<OptionDetailedResponseView> options,
            ResponseDetailView response
    ) {}

    public record OptionDetailedResponseView(
            long optionId,
            String code,
            String text,
            int order,
            BigDecimal ordinalValue,
            List<OptionImagePayload> images,
            boolean isCorrect,
            boolean selected
    ) {}

    public record ResponseDetailView(
            Long respuestaId,
            String textAnswer,
            BigDecimal numericAnswer,
            Integer timeUsedSeconds,
            LocalDateTime answeredAt,
            Boolean graded,
            BigDecimal scoreObtained,
            Boolean isCorrect,
            Boolean requiresManualReview,
            String observation
    ) {}

    public record ItemImagePayload(long imageId, String url, String role, int order, String altText) {
    }

    public record OptionImagePayload(long imageId, String url, int order, String altText) {
    }
}
