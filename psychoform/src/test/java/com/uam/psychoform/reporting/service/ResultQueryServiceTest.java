package com.uam.psychoform.reporting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uam.psychoform.assessment.model.AsignacionTest;
import com.uam.psychoform.assessment.model.IntentoTest;
import com.uam.psychoform.assessment.model.SesionAplicacion;
import com.uam.psychoform.assessment.repository.AsignacionTestRepository;
import com.uam.psychoform.assessment.repository.IntentoTestRepository;
import com.uam.psychoform.assessment.repository.SesionSubtestRepository;
import com.uam.psychoform.assessment.repository.RespuestaItemRepository;
import com.uam.psychoform.assessment.repository.OpcionSeleccionadaRespuestaRepository;
import com.uam.psychoform.instrument.model.Baremo;
import com.uam.psychoform.instrument.model.DimensionResultado;
import com.uam.psychoform.instrument.model.RangoBaremo;
import com.uam.psychoform.instrument.model.VersionTest;
import com.uam.psychoform.instrument.repository.BaremoRepository;
import com.uam.psychoform.instrument.repository.ItemLookupRepository;
import com.uam.psychoform.instrument.repository.OpcionItemLookupRepository;
import com.uam.psychoform.instrument.repository.ClaveRespuestaLookupRepository;
import com.uam.psychoform.instrument.repository.ImagenItemRepository;
import com.uam.psychoform.instrument.repository.ImagenOpcionRepository;
import com.uam.psychoform.instrument.repository.RangoBaremoRepository;
import com.uam.psychoform.scoring.model.Resultado;
import com.uam.psychoform.scoring.model.ResultadoDimension;
import com.uam.psychoform.scoring.repository.ResultadoDimensionRepository;
import com.uam.psychoform.scoring.repository.ResultadoRepository;
import com.uam.psychoform.scoring.repository.CalificacionRespuestaRepository;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.prepost.PreAuthorize;

class ResultQueryServiceTest {
    private final AsignacionTestRepository asignaciones = Mockito.mock(AsignacionTestRepository.class);
    private final IntentoTestRepository intentos = Mockito.mock(IntentoTestRepository.class);
    private final ResultadoRepository resultados = Mockito.mock(ResultadoRepository.class);
    private final ResultadoDimensionRepository dimensiones = Mockito.mock(ResultadoDimensionRepository.class);
    private final SesionSubtestRepository sesionSubtests = Mockito.mock(SesionSubtestRepository.class);
    private final ItemLookupRepository items = Mockito.mock(ItemLookupRepository.class);
    private final OpcionItemLookupRepository opciones = Mockito.mock(OpcionItemLookupRepository.class);
    private final ClaveRespuestaLookupRepository claves = Mockito.mock(ClaveRespuestaLookupRepository.class);
    private final BaremoRepository baremos = Mockito.mock(BaremoRepository.class);
    private final RangoBaremoRepository rangosBaremo = Mockito.mock(RangoBaremoRepository.class);
    private final RespuestaItemRepository respuestas = Mockito.mock(RespuestaItemRepository.class);
    private final OpcionSeleccionadaRespuestaRepository opcionesSeleccionadas = Mockito.mock(OpcionSeleccionadaRespuestaRepository.class);
    private final CalificacionRespuestaRepository calificaciones = Mockito.mock(CalificacionRespuestaRepository.class);
    private final ImagenItemRepository imagenesItem = Mockito.mock(ImagenItemRepository.class);
    private final ImagenOpcionRepository imagenesOpcion = Mockito.mock(ImagenOpcionRepository.class);

    private final ResultQueryService service = new ResultQueryService(
            asignaciones, intentos, resultados, dimensiones,
            sesionSubtests, items, opciones, claves,
            baremos, rangosBaremo,
            respuestas, opcionesSeleccionadas, calificaciones,
            imagenesItem, imagenesOpcion
    );

    @Test
    void getAttemptResultRequierePermisoResultadoVer() throws Exception {
        Method method = ResultQueryService.class.getMethod("getAttemptResult", long.class);

        assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAuthority('PERM_RESULTADO_VER')");
    }

    @Test
    void getAttemptResultDevuelveDtoSanitizado() {
        Resultado resultado = new Resultado();
        resultado.setId(8L);
        resultado.setEstado("CALCULADO");
        resultado.setPuntajeTotalDirecto(new BigDecimal("42.00"));
        resultado.setIntento(intentoConVersion(100L));
        DimensionResultado dimension = new DimensionResultado();
        dimension.setId(3L);
        dimension.setNombreDimension("Espacial");
        ResultadoDimension resultadoDimension = new ResultadoDimension();
        resultadoDimension.setDimensionResultado(dimension);
        resultadoDimension.setPuntajeDirecto(new BigDecimal("14.00"));
        resultadoDimension.setCategoria("Alto");
        resultadoDimension.setPercentil(new BigDecimal("75"));
        resultadoDimension.setInterpretacion("Interpretacion aprobada");
        when(resultados.findByIntentoId(7L)).thenReturn(Optional.of(resultado));
        when(dimensiones.findByResultadoId(8L)).thenReturn(List.of(resultadoDimension));
        when(baremos.findPreferredTotalBaremo(100L)).thenReturn(Optional.empty());

        ResultQueryService.IndividualResultView view = service.getAttemptResult(7L);

        assertThat(view.resultId()).isEqualTo(8L);
        assertThat(view.totalScore()).isEqualByComparingTo("42.00");
        assertThat(view.dimensions()).singleElement().satisfies(d -> {
            assertThat(d.name()).isEqualTo("Espacial");
            assertThat(d.category()).isEqualTo("Alto");
        });
        assertThat(view.disclaimer()).contains("no emite diagnosticos");
    }

    @Test
    void getAttemptResultInterpretaPuntajeTotalConBaremoGeneral() {
        Resultado resultado = new Resultado();
        resultado.setId(8L);
        resultado.setEstado("CALCULADO");
        resultado.setPuntajeTotalDirecto(new BigDecimal("100.00"));
        resultado.setIntento(intentoConVersion(3L));
        Baremo baremo = new Baremo();
        baremo.setId(6L);
        baremo.setCodigoBaremo("GENERAL");
        RangoBaremo rango = new RangoBaremo();
        rango.setId(8L);
        rango.setBaremo(baremo);
        rango.setCategoria("IQ 100");
        rango.setPercentil(new BigDecimal("100.00"));
        rango.setInterpretacion("Duro");
        rango.setRecomendacion(null);
        when(resultados.findByIntentoId(5L)).thenReturn(Optional.of(resultado));
        when(dimensiones.findByResultadoId(8L)).thenReturn(List.of());
        when(baremos.findPreferredTotalBaremo(3L)).thenReturn(Optional.of(baremo));
        when(rangosBaremo.findMatchingRange(6L, new BigDecimal("100.00"))).thenReturn(Optional.of(rango));

        ResultQueryService.IndividualResultView view = service.getAttemptResult(5L);

        assertThat(view.dimensions()).isEmpty();
        assertThat(view.totalInterpretation()).isNotNull();
        assertThat(view.totalInterpretation().baremoCode()).isEqualTo("GENERAL");
        assertThat(view.totalInterpretation().category()).isEqualTo("IQ 100");
        assertThat(view.totalInterpretation().percentile()).isEqualByComparingTo("100.00");
        assertThat(view.totalInterpretation().interpretation()).isEqualTo("Duro");
    }

    @Test
    void getDimensionAveragesCargaDimensionesEnBatchParaEvitarConsultaPorResultado() {
        Resultado first = new Resultado();
        first.setId(8L);
        Resultado second = new Resultado();
        second.setId(9L);
        DimensionResultado dimension = new DimensionResultado();
        dimension.setId(3L);
        dimension.setNombreDimension("Espacial");
        ResultadoDimension firstDimension = new ResultadoDimension();
        firstDimension.setDimensionResultado(dimension);
        firstDimension.setPuntajeDirecto(new BigDecimal("14.00"));
        ResultadoDimension secondDimension = new ResultadoDimension();
        secondDimension.setDimensionResultado(dimension);
        secondDimension.setPuntajeDirecto(new BigDecimal("16.00"));
        when(resultados.findBySessionId(30L)).thenReturn(List.of(first, second));
        when(dimensiones.findByResultadoIdIn(List.of(8L, 9L))).thenReturn(List.of(firstDimension, secondDimension));

        List<ResultQueryService.DimensionAggregateView> averages =
                service.getDimensionAverages(new ResultQueryService.ResultFilter(30L));

        assertThat(averages).singleElement().satisfies(row -> {
            assertThat(row.dimensionId()).isEqualTo(3L);
            assertThat(row.count()).isEqualTo(2);
        });
        verify(dimensiones).findByResultadoIdIn(List.of(8L, 9L));
        verify(dimensiones, never()).findByResultadoId(8L);
        verify(dimensiones, never()).findByResultadoId(9L);
    }

    @Test
    void getDetailedAttemptResultConResultadoInexistente() {
        IntentoTest intento = Mockito.mock(IntentoTest.class);
        AsignacionTest asignacion = Mockito.mock(AsignacionTest.class);
        com.uam.psychoform.academic.model.Participante participante = Mockito.mock(com.uam.psychoform.academic.model.Participante.class);
        com.uam.psychoform.assessment.model.SesionAplicacion sesion = Mockito.mock(com.uam.psychoform.assessment.model.SesionAplicacion.class);
        com.uam.psychoform.instrument.model.VersionTest version = Mockito.mock(com.uam.psychoform.instrument.model.VersionTest.class);
        com.uam.psychoform.instrument.model.TestPsicologico test = Mockito.mock(com.uam.psychoform.instrument.model.TestPsicologico.class);

        when(intento.getAsignacion()).thenReturn(asignacion);
        when(asignacion.getParticipante()).thenReturn(participante);
        when(asignacion.getSesionAplicacion()).thenReturn(sesion);
        when(sesion.getVersionTest()).thenReturn(version);
        when(version.getTest()).thenReturn(test);

        when(participante.getNombres()).thenReturn("Gabriel");
        when(participante.getApellidos()).thenReturn("Pérez");
        when(participante.getCodigoParticipante()).thenReturn("2026-0001");
        when(intento.getEstado()).thenReturn(com.uam.psychoform.assessment.model.EstadoIntento.COMPLETADO);
        when(test.getNombreTest()).thenReturn("BFA");
        when(version.getNumeroVersion()).thenReturn("v1.0");

        when(intentos.findByIdWithAsignacionAndParticipanteAndSesion(7L)).thenReturn(Optional.of(intento));
        when(resultados.findByIntentoId(7L)).thenReturn(Optional.empty());
        when(sesionSubtests.findBySesionAplicacionIdWithSubtestOrderByNumeroOrdenAsc(Mockito.anyLong())).thenReturn(List.of());

        ResultQueryService.DetailedAttemptResultView view = service.getDetailedAttemptResult(7L);

        assertThat(view.participantName()).isEqualTo("Gabriel Pérez");
        assertThat(view.participantCode()).isEqualTo("2026-0001");
        assertThat(view.totalScore()).isNull();
    }

    @Test
    void getDetailedAttemptResultConResultadoExistente() {
        IntentoTest intento = Mockito.mock(IntentoTest.class);
        AsignacionTest asignacion = Mockito.mock(AsignacionTest.class);
        com.uam.psychoform.academic.model.Participante participante = Mockito.mock(com.uam.psychoform.academic.model.Participante.class);
        com.uam.psychoform.assessment.model.SesionAplicacion sesion = Mockito.mock(com.uam.psychoform.assessment.model.SesionAplicacion.class);
        com.uam.psychoform.instrument.model.VersionTest version = Mockito.mock(com.uam.psychoform.instrument.model.VersionTest.class);
        com.uam.psychoform.instrument.model.TestPsicologico test = Mockito.mock(com.uam.psychoform.instrument.model.TestPsicologico.class);

        when(intento.getAsignacion()).thenReturn(asignacion);
        when(asignacion.getParticipante()).thenReturn(participante);
        when(asignacion.getSesionAplicacion()).thenReturn(sesion);
        when(sesion.getVersionTest()).thenReturn(version);
        when(version.getTest()).thenReturn(test);

        when(participante.getNombres()).thenReturn("Silvio");
        when(participante.getApellidos()).thenReturn("Mora");
        when(participante.getCodigoParticipante()).thenReturn("2026-0002");
        when(intento.getEstado()).thenReturn(com.uam.psychoform.assessment.model.EstadoIntento.COMPLETADO);
        when(test.getNombreTest()).thenReturn("BFA");
        when(version.getNumeroVersion()).thenReturn("v1.0");

        Resultado resultado = new Resultado();
        resultado.setId(8L);
        resultado.setPuntajeTotalDirecto(new BigDecimal("35.00"));
        resultado.setEstado("CALCULADO");

        when(intentos.findByIdWithAsignacionAndParticipanteAndSesion(7L)).thenReturn(Optional.of(intento));
        when(resultados.findByIntentoId(7L)).thenReturn(Optional.of(resultado));
        when(dimensiones.findByResultadoId(8L)).thenReturn(List.of());
        when(baremos.findPreferredTotalBaremo(Mockito.anyLong())).thenReturn(Optional.empty());
        when(calificaciones.findByResultadoId(8L)).thenReturn(List.of());
        when(sesionSubtests.findBySesionAplicacionIdWithSubtestOrderByNumeroOrdenAsc(Mockito.anyLong())).thenReturn(List.of());

        ResultQueryService.DetailedAttemptResultView view = service.getDetailedAttemptResult(7L);

        assertThat(view.participantName()).isEqualTo("Silvio Mora");
        assertThat(view.totalScore()).isEqualByComparingTo("35.00");
    }

    private static IntentoTest intentoConVersion(Long versionId) {
        VersionTest version = new VersionTest();
        version.setId(versionId);
        SesionAplicacion sesion = new SesionAplicacion();
        sesion.setVersionTest(version);
        AsignacionTest asignacion = new AsignacionTest();
        asignacion.setSesionAplicacion(sesion);
        IntentoTest intento = new IntentoTest();
        intento.setAsignacion(asignacion);
        return intento;
    }
}
