package com.uam.psychoform.scoring.repository;

import com.uam.psychoform.scoring.model.CalificacionRespuesta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalificacionRespuestaRepository extends JpaRepository<CalificacionRespuesta, Long> {
    List<CalificacionRespuesta> findByResultadoId(Long resultadoId);
}

