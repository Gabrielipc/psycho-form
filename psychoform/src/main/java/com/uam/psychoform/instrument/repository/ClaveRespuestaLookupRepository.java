package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.model.ClaveRespuesta;
import com.uam.psychoform.instrument.model.EstadoConfiguracion;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClaveRespuestaLookupRepository extends JpaRepository<ClaveRespuesta, Long> {
    default List<ClaveRespuesta> findOfficialKeysByVersionId(Long versionTestId) {
        return findOfficialKeysByVersionIdAndEstados(versionTestId,
                List.of(EstadoConfiguracion.APROBADO, EstadoConfiguracion.PUBLICADO));
    }

    @Query("""
            select c from ClaveRespuesta c
            join c.reglaCalificacion r
            where r.versionTest.id = :versionTestId
              and r.activa = true
              and r.estado in :estados
            """)
    List<ClaveRespuesta> findOfficialKeysByVersionIdAndEstados(Long versionTestId,
            Collection<EstadoConfiguracion> estados);
}
