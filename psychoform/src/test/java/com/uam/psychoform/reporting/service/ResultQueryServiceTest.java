package com.uam.psychoform.reporting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uam.psychoform.assessment.repository.AsignacionTestRepository;
import com.uam.psychoform.assessment.repository.IntentoTestRepository;
import com.uam.psychoform.instrument.model.DimensionResultado;
import com.uam.psychoform.scoring.model.Resultado;
import com.uam.psychoform.scoring.model.ResultadoDimension;
import com.uam.psychoform.scoring.repository.ResultadoDimensionRepository;
import com.uam.psychoform.scoring.repository.ResultadoRepository;
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
    private final ResultQueryService service = new ResultQueryService(asignaciones, intentos, resultados, dimensiones);

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
}
