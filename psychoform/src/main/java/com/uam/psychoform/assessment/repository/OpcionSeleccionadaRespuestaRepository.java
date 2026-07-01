package com.uam.psychoform.assessment.repository;

import com.uam.psychoform.assessment.model.OpcionSeleccionadaRespuesta;
import com.uam.psychoform.assessment.model.OpcionSeleccionadaRespuestaId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OpcionSeleccionadaRespuestaRepository
        extends JpaRepository<OpcionSeleccionadaRespuesta, OpcionSeleccionadaRespuestaId> {
    void deleteByRespuestaId(Long respuestaId);

    List<OpcionSeleccionadaRespuesta> findByRespuestaId(Long respuestaId);

    @Query("""
            select osr from OpcionSeleccionadaRespuesta osr
            join fetch osr.respuesta
            join fetch osr.opcion
            where osr.respuesta.id in :respuestaIds
            """)
    List<OpcionSeleccionadaRespuesta> findByRespuestaIdIn(Collection<Long> respuestaIds);
}
