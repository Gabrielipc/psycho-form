package com.uam.psychoform.assessment.repository;

import com.uam.psychoform.assessment.model.OpcionSeleccionadaRespuesta;
import com.uam.psychoform.assessment.model.OpcionSeleccionadaRespuestaId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpcionSeleccionadaRespuestaRepository
        extends JpaRepository<OpcionSeleccionadaRespuesta, OpcionSeleccionadaRespuestaId> {
    void deleteByRespuestaId(Long respuestaId);

    List<OpcionSeleccionadaRespuesta> findByRespuestaId(Long respuestaId);
}
