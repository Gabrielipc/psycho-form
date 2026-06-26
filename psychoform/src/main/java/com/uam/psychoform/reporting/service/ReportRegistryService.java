package com.uam.psychoform.reporting.service;

import com.uam.psychoform.assessment.model.IntentoTest;
import com.uam.psychoform.assessment.model.SesionAplicacion;
import com.uam.psychoform.assessment.repository.IntentoTestRepository;
import com.uam.psychoform.assessment.repository.SesionAplicacionRepository;
import com.uam.psychoform.reporting.model.FormatoReporte;
import com.uam.psychoform.reporting.model.ReporteGenerado;
import com.uam.psychoform.reporting.repository.ReporteGeneradoRepository;
import com.uam.psychoform.scoring.model.Resultado;
import com.uam.psychoform.scoring.repository.ResultadoRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.SecurityPermissions;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportRegistryService {
    private final ReporteGeneradoRepository repository;
    private final IntentoTestRepository intentos;
    private final ResultadoRepository resultados;
    private final SesionAplicacionRepository sesiones;
    private final UsuarioRepository usuarios;
    private final CurrentActor currentActor;
    private final Clock clock;

    public ReportRegistryService(ReporteGeneradoRepository repository, IntentoTestRepository intentos,
            ResultadoRepository resultados, SesionAplicacionRepository sesiones, UsuarioRepository usuarios,
            CurrentActor currentActor, Clock clock) {
        this.repository = repository;
        this.intentos = intentos;
        this.resultados = resultados;
        this.sesiones = sesiones;
        this.usuarios = usuarios;
        this.currentActor = currentActor;
        this.clock = clock;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.REPORTE_EXPORTAR)
    public ReporteGenerado registerIndividualReport(RegisterReportCommand command) {
        if (command.intentoId() == null && command.resultadoId() == null) {
            throw new IllegalArgumentException("El reporte individual requiere intento o resultado");
        }
        ReporteGenerado report = baseReport(command);
        if (command.intentoId() != null) {
            IntentoTest intento = intentos.findById(command.intentoId())
                    .orElseThrow(() -> new EntityNotFoundException("Intento no encontrado: " + command.intentoId()));
            report.setIntento(intento);
        }
        if (command.resultadoId() != null) {
            Resultado resultado = resultados.findById(command.resultadoId())
                    .orElseThrow(() -> new EntityNotFoundException("Resultado no encontrado: " + command.resultadoId()));
            report.setResultado(resultado);
        }
        repository.save(report);
        return report;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.REPORTE_EXPORTAR)
    public ReporteGenerado registerAggregateReport(RegisterReportCommand command) {
        if (command.sesionAplicacionId() == null) {
            throw new IllegalArgumentException("El reporte agregado requiere sesion");
        }
        ReporteGenerado report = baseReport(command);
        SesionAplicacion sesion = sesiones.findById(command.sesionAplicacionId())
                .orElseThrow(() -> new EntityNotFoundException("Sesion no encontrada: " + command.sesionAplicacionId()));
        report.setSesionAplicacion(sesion);
        repository.save(report);
        return report;
    }

    @PreAuthorize(SecurityPermissions.REPORTE_LEER)
    public List<ReporteGenerado> listReports(ReportFilter filter) {
        if (filter.sesionAplicacionId() != null) {
            return repository.findBySesionAplicacionId(filter.sesionAplicacionId());
        }
        if (filter.resultadoId() != null) {
            return repository.findByResultadoId(filter.resultadoId());
        }
        if (filter.intentoId() != null) {
            return repository.findByIntentoId(filter.intentoId());
        }
        return repository.findAll();
    }

    private ReporteGenerado baseReport(RegisterReportCommand command) {
        ReporteGenerado report = new ReporteGenerado();
        report.setTipoReporte(command.tipoReporte());
        report.setFormato(command.formato());
        report.setRutaAlmacenamiento(command.rutaAlmacenamiento());
        report.setResumenFiltros(command.resumenFiltrosJson());
        report.setGeneradoPor(currentUser());
        report.setGeneradoEn(LocalDateTime.now(clock));
        return report;
    }

    private Usuario currentUser() {
        UUID actorId = currentActor.usuarioId();
        return usuarios.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado: " + actorId));
    }

    public record RegisterReportCommand(String tipoReporte, FormatoReporte formato, String rutaAlmacenamiento,
            String resumenFiltrosJson, Long intentoId, Long resultadoId, Long sesionAplicacionId) {
        public RegisterReportCommand(String tipoReporte, FormatoReporte formato, String rutaAlmacenamiento,
                String resumenFiltrosJson) {
            this(tipoReporte, formato, rutaAlmacenamiento, resumenFiltrosJson, null, null, null);
        }
    }

    public record ReportFilter(Long intentoId, Long resultadoId, Long sesionAplicacionId) {
    }
}
