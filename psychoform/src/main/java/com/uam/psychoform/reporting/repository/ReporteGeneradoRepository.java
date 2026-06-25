package com.uam.psychoform.reporting.repository;

import com.uam.psychoform.reporting.entity.ReporteGenerado;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteGeneradoRepository extends JpaRepository<ReporteGenerado, Long> {
    List<ReporteGenerado> findBySesionAplicacionId(Long sesionAplicacionId);

    List<ReporteGenerado> findByResultadoId(Long resultadoId);

    List<ReporteGenerado> findByIntentoId(Long intentoId);
}
