package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.entity.ClaveRespuesta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClaveRespuestaLookupRepository extends JpaRepository<ClaveRespuesta, Long> {
    @Query("""
            select c from ClaveRespuesta c
            join c.reglaCalificacion r
            where r.versionTest.id = :versionTestId
              and r.activa = true
              and r.estado in (com.uam.psychoform.instrument.entity.EstadoConfiguracion.APROBADO,
                               com.uam.psychoform.instrument.entity.EstadoConfiguracion.PUBLICADO)
            """)
    List<ClaveRespuesta> findOfficialKeysByVersionId(Long versionTestId);
}
