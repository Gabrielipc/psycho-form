package com.uam.psychoform.reporting.service;

import com.uam.psychoform.assessment.entity.EstadoAsignacion;
import com.uam.psychoform.assessment.entity.EstadoIntento;
import com.uam.psychoform.assessment.repository.AsignacionTestRepository;
import com.uam.psychoform.assessment.repository.IntentoTestRepository;
import com.uam.psychoform.scoring.entity.Resultado;
import com.uam.psychoform.scoring.entity.ResultadoDimension;
import com.uam.psychoform.scoring.repository.ResultadoDimensionRepository;
import com.uam.psychoform.scoring.repository.ResultadoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    public ResultQueryService(AsignacionTestRepository asignaciones, IntentoTestRepository intentos,
            ResultadoRepository resultados, ResultadoDimensionRepository dimensiones) {
        this.asignaciones = asignaciones;
        this.intentos = intentos;
        this.resultados = resultados;
        this.dimensiones = dimensiones;
    }

    @PreAuthorize("hasAuthority('PERM_RESULTADO_VER')")
    public IndividualResultView getAttemptResult(long attemptId) {
        Resultado resultado = resultados.findByIntentoId(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Resultado no encontrado para intento: " + attemptId));
        List<DimensionResultView> dimensionViews = dimensiones.findByResultadoId(resultado.getId()).stream()
                .map(d -> new DimensionResultView(d.getDimensionResultado().getId(),
                        d.getDimensionResultado().getNombreDimension(), d.getPuntajeDirecto(), d.getCategoria(),
                        d.getPercentil(), d.getInterpretacion()))
                .toList();
        return new IndividualResultView(attemptId, resultado.getId(), resultado.getEstado(),
                resultado.getPuntajeTotalDirecto(), dimensionViews, DISCLAIMER);
    }

    @PreAuthorize("hasAuthority('PERM_RESULTADO_AGREGADO_VER') or hasAuthority('PERM_RESULTADO_VER')")
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

    @PreAuthorize("hasAuthority('PERM_RESULTADO_AGREGADO_VER') or hasAuthority('PERM_RESULTADO_VER')")
    public List<DimensionAggregateView> getDimensionAverages(ResultFilter filter) {
        List<Resultado> sessionResults = resultados.findBySessionId(filter.sessionId());
        Collection<Long> resultIds = sessionResults.stream().map(Resultado::getId).toList();
        if (resultIds.isEmpty()) {
            return List.of();
        }
        Map<Long, List<ResultadoDimension>> byDimension = resultIds.stream()
                .flatMap(resultId -> dimensiones.findByResultadoId(resultId).stream())
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
            List<DimensionResultView> dimensions, String disclaimer) {
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
}
