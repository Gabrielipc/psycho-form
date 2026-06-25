package com.uam.psychoform.reporting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uam.psychoform.assessment.entity.SesionAplicacion;
import com.uam.psychoform.assessment.repository.IntentoTestRepository;
import com.uam.psychoform.assessment.repository.SesionAplicacionRepository;
import com.uam.psychoform.reporting.entity.FormatoReporte;
import com.uam.psychoform.reporting.entity.ReporteGenerado;
import com.uam.psychoform.reporting.repository.ReporteGeneradoRepository;
import com.uam.psychoform.scoring.repository.ResultadoRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.entity.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.prepost.PreAuthorize;

class ReportRegistryServiceTest {
    private final ReporteGeneradoRepository repository = Mockito.mock(ReporteGeneradoRepository.class);
    private final IntentoTestRepository intentos = Mockito.mock(IntentoTestRepository.class);
    private final ResultadoRepository resultados = Mockito.mock(ResultadoRepository.class);
    private final SesionAplicacionRepository sesiones = Mockito.mock(SesionAplicacionRepository.class);
    private final UsuarioRepository usuarios = Mockito.mock(UsuarioRepository.class);
    private final CurrentActor currentActor = Mockito.mock(CurrentActor.class);
    private final ReportRegistryService service = new ReportRegistryService(repository, intentos, resultados, sesiones,
            usuarios, currentActor,
            Clock.fixed(Instant.parse("2026-06-24T12:00:00Z"), ZoneOffset.UTC));

    @Test
    void registerAggregateReportRequierePermisoExportar() throws Exception {
        Method method = ReportRegistryService.class.getMethod("registerAggregateReport",
                ReportRegistryService.RegisterReportCommand.class);

        assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAuthority('PERM_REPORTE_EXPORTAR')");
    }

    @Test
    void registerAggregateReportGuardaMetadataYActorActual() {
        UUID actorId = UUID.randomUUID();
        Usuario actor = new Usuario();
        actor.setId(actorId);
        SesionAplicacion sesion = new SesionAplicacion();
        sesion.setId(10L);
        when(currentActor.usuarioId()).thenReturn(actorId);
        when(usuarios.findById(actorId)).thenReturn(Optional.of(actor));
        when(sesiones.findById(10L)).thenReturn(Optional.of(sesion));

        ReporteGenerado report = service.registerAggregateReport(new ReportRegistryService.RegisterReportCommand(
                "AGREGADO_SESION", FormatoReporte.CSV, "/tmp/report.csv", "{\"sesion\":10}", null, null, 10L));

        assertThat(report.getGeneradoPor()).isSameAs(actor);
        assertThat(report.getSesionAplicacion()).isSameAs(sesion);
        assertThat(report.getTipoReporte()).isEqualTo("AGREGADO_SESION");
        assertThat(report.getFormato()).isEqualTo(FormatoReporte.CSV);
        verify(repository).save(report);
    }
}
