package com.uam.psychoform.scoring.service;

import com.uam.psychoform.assessment.model.EstadoIntento;
import com.uam.psychoform.assessment.model.IntentoTest;
import com.uam.psychoform.assessment.model.OpcionSeleccionadaRespuesta;
import com.uam.psychoform.assessment.model.RespuestaItem;
import com.uam.psychoform.assessment.repository.IntentoTestRepository;
import com.uam.psychoform.assessment.repository.OpcionSeleccionadaRespuestaRepository;
import com.uam.psychoform.assessment.repository.RespuestaItemRepository;
import com.uam.psychoform.instrument.model.ClaveRespuesta;
import com.uam.psychoform.instrument.model.DimensionResultado;
import com.uam.psychoform.instrument.model.EstadoVersionTest;
import com.uam.psychoform.instrument.model.OpcionPuntajeDimension;
import com.uam.psychoform.instrument.model.RangoBaremo;
import com.uam.psychoform.instrument.model.TipoEstrategiaCalificacion;
import com.uam.psychoform.instrument.model.VersionTest;
import com.uam.psychoform.instrument.repository.BaremoRepository;
import com.uam.psychoform.instrument.repository.ClaveRespuestaLookupRepository;
import com.uam.psychoform.instrument.repository.OpcionPuntajeDimensionRepository;
import com.uam.psychoform.instrument.repository.RangoBaremoRepository;
import com.uam.psychoform.scoring.model.CalificacionRespuesta;
import com.uam.psychoform.scoring.model.Resultado;
import com.uam.psychoform.scoring.model.ResultadoDimension;
import com.uam.psychoform.scoring.repository.CalificacionRespuestaRepository;
import com.uam.psychoform.scoring.repository.ResultadoDimensionRepository;
import com.uam.psychoform.scoring.repository.ResultadoRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ClaveSimpleScoringService {
    private final IntentoTestRepository intentos;
    private final ResultadoRepository resultados;
    private final RespuestaItemRepository respuestas;
    private final OpcionSeleccionadaRespuestaRepository opcionesSeleccionadas;
    private final ClaveRespuestaLookupRepository claves;
    private final OpcionPuntajeDimensionRepository puntajesDimension;
    private final BaremoRepository baremos;
    private final RangoBaremoRepository rangosBaremo;
    private final CalificacionRespuestaRepository calificaciones;
    private final ResultadoDimensionRepository resultadosDimension;
    private final UsuarioRepository usuarios;
    private final CurrentActor currentActor;
    private final Clock clock;

    public ClaveSimpleScoringService(IntentoTestRepository intentos, ResultadoRepository resultados,
            RespuestaItemRepository respuestas, OpcionSeleccionadaRespuestaRepository opcionesSeleccionadas,
            ClaveRespuestaLookupRepository claves, OpcionPuntajeDimensionRepository puntajesDimension,
            BaremoRepository baremos, RangoBaremoRepository rangosBaremo,
            CalificacionRespuestaRepository calificaciones, ResultadoDimensionRepository resultadosDimension,
            UsuarioRepository usuarios, CurrentActor currentActor, Clock clock) {
        this.intentos = intentos;
        this.resultados = resultados;
        this.respuestas = respuestas;
        this.opcionesSeleccionadas = opcionesSeleccionadas;
        this.claves = claves;
        this.puntajesDimension = puntajesDimension;
        this.baremos = baremos;
        this.rangosBaremo = rangosBaremo;
        this.calificaciones = calificaciones;
        this.resultadosDimension = resultadosDimension;
        this.usuarios = usuarios;
        this.currentActor = currentActor;
        this.clock = clock;
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_CALIFICACION_EJECUTAR')")
    public Resultado scoreAttempt(long intentoId) {
        return resultados.findByIntentoId(intentoId).orElseGet(() -> calculate(intentoId));
    }

    private Resultado calculate(long intentoId) {
        IntentoTest intento = intentos.findByIdForUpdate(intentoId)
                .orElseThrow(() -> new EntityNotFoundException("Intento no encontrado: " + intentoId));
        if (intento.getEstado() != EstadoIntento.COMPLETADO) {
            throw new IllegalStateException("El intento debe estar finalizado para calificar");
        }
        VersionTest version = intento.getAsignacion().getSesionAplicacion().getVersionTest();
        if (version.getEstado() != EstadoVersionTest.PUBLICADO) {
            throw new IllegalStateException("La version del test no esta publicada");
        }
        if (version.getEstrategiaCalificacion() == null
                || version.getEstrategiaCalificacion().getTipoEstrategia() != TipoEstrategiaCalificacion.CLAVE_SIMPLE) {
            throw new IllegalStateException("La version no usa CLAVE_SIMPLE");
        }
        UUID actorId = currentActor.usuarioId();
        Usuario actor = usuarios.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado: " + actorId));
        Map<Long, ClaveRespuesta> keysByItem = claves.findOfficialKeysByVersionId(version.getId()).stream()
                .collect(Collectors.toMap(c -> c.getItem().getId(), Function.identity()));
        Map<Long, BigDecimal> directScoresByDimension = new java.util.LinkedHashMap<>();
        Map<Long, DimensionResultado> dimensionsById = new java.util.LinkedHashMap<>();

        Resultado resultado = new Resultado();
        resultado.setIntento(intento);
        resultado.setEstrategiaCalificacion(version.getEstrategiaCalificacion());
        resultado.setCalculadoPor(actor);
        resultado.setCalculadoEn(LocalDateTime.now(clock));
        resultado.setPuntajeTotalDirecto(BigDecimal.ZERO);
        resultado.setCantidadItems(0);
        resultado.setCantidadCorrectas(0);
        resultado.setCantidadIncorrectas(0);
        resultado.setCantidadPendientesRevision(0);
        resultado.setRequiereRevisionManual(false);
        resultado.setEstado("CALCULADO");
        resultados.save(resultado);

        for (RespuestaItem respuesta : respuestas.findByIntentoId(intentoId)) {
            ClaveRespuesta clave = keysByItem.get(respuesta.getItem().getId());
            if (clave == null) {
                continue;
            }
            List<Long> selectedOptionIds = opcionesSeleccionadas.findByRespuestaId(respuesta.getId()).stream()
                    .map(OpcionSeleccionadaRespuesta::getOpcion)
                    .map(o -> o.getId())
                    .toList();
            accumulateDimensionScores(version.getId(), selectedOptionIds, directScoresByDimension, dimensionsById);
            boolean correct = clave.getOpcionCorrecta() != null && selectedOptionIds.contains(clave.getOpcionCorrecta().getId());
            BigDecimal score = correct ? clave.getPuntaje() : BigDecimal.ZERO;
            resultado.setCantidadItems(resultado.getCantidadItems() + 1);
            resultado.setPuntajeTotalDirecto(resultado.getPuntajeTotalDirecto().add(score));
            resultado.setCantidadCorrectas(resultado.getCantidadCorrectas() + (correct ? 1 : 0));
            resultado.setCantidadIncorrectas(resultado.getCantidadIncorrectas() + (correct ? 0 : 1));
            if (Boolean.TRUE.equals(clave.getRequiereRevisionManual())) {
                resultado.setCantidadPendientesRevision(resultado.getCantidadPendientesRevision() + 1);
                resultado.setRequiereRevisionManual(true);
            }
            CalificacionRespuesta calificacion = new CalificacionRespuesta();
            calificacion.setResultado(resultado);
            calificacion.setRespuesta(respuesta);
            calificacion.setReglaCalificacion(clave.getReglaCalificacion());
            calificacion.setOpcion(clave.getOpcionCorrecta());
            calificacion.setPuntajeObtenido(score);
            calificacion.setEsCorrecta(correct);
            calificacion.setRequiereRevisionManual(Boolean.TRUE.equals(clave.getRequiereRevisionManual()));
            calificacion.setCreadoEn(LocalDateTime.now(clock));
            calificaciones.save(calificacion);
        }
        createDimensionResults(resultado, version.getId(), directScoresByDimension, dimensionsById);
        resultados.save(resultado);
        return resultado;
    }

    private void accumulateDimensionScores(Long versionTestId, List<Long> selectedOptionIds,
            Map<Long, BigDecimal> directScoresByDimension, Map<Long, DimensionResultado> dimensionsById) {
        if (selectedOptionIds.isEmpty()) {
            return;
        }
        for (OpcionPuntajeDimension matrixRow : puntajesDimension
                .findActiveOfficialByVersionAndOptionIds(versionTestId, selectedOptionIds)) {
            Long dimensionId = matrixRow.getDimensionResultado().getId();
            BigDecimal contribution = matrixRow.getPuntaje().multiply(matrixRow.getPeso());
            directScoresByDimension.merge(dimensionId, contribution, BigDecimal::add);
            dimensionsById.putIfAbsent(dimensionId, matrixRow.getDimensionResultado());
        }
    }

    private void createDimensionResults(Resultado resultado, Long versionTestId,
            Map<Long, BigDecimal> directScoresByDimension, Map<Long, DimensionResultado> dimensionsById) {
        for (Map.Entry<Long, BigDecimal> entry : directScoresByDimension.entrySet()) {
            ResultadoDimension dimensionResult = new ResultadoDimension();
            dimensionResult.setResultado(resultado);
            dimensionResult.setDimensionResultado(dimensionsById.get(entry.getKey()));
            dimensionResult.setPuntajeDirecto(entry.getValue());
            dimensionResult.setRequiereRevisionManual(resultado.getRequiereRevisionManual());

            baremos.findPreferredDimensionBaremo(versionTestId, entry.getKey()).ifPresent(baremo -> {
                dimensionResult.setBaremo(baremo);
                Optional<RangoBaremo> matchingRange = rangosBaremo.findMatchingRange(baremo.getId(), entry.getValue());
                matchingRange.ifPresent(range -> applyRange(dimensionResult, range));
            });
            resultadosDimension.save(dimensionResult);
        }
    }

    private static void applyRange(ResultadoDimension dimensionResult, RangoBaremo range) {
        dimensionResult.setRangoBaremo(range);
        dimensionResult.setCategoria(range.getCategoria());
        dimensionResult.setPercentil(range.getPercentil());
        dimensionResult.setInterpretacion(range.getInterpretacion());
        dimensionResult.setPuntajeTransformado(range.getPercentil());
    }
}
