package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.model.OpcionPuntajeDimension;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OpcionPuntajeDimensionRepository extends JpaRepository<OpcionPuntajeDimension, Long> {
    @Query("""
            select opd from OpcionPuntajeDimension opd
            join fetch opd.opcion
            join fetch opd.dimensionResultado
            join fetch opd.reglaCalificacion regla
            where opd.activa = true
              and opd.opcion.id in :optionIds
              and regla.versionTest.id = :versionTestId
              and regla.activa = true
              and regla.estado in (com.uam.psychoform.instrument.model.EstadoConfiguracion.APROBADO,
                                   com.uam.psychoform.instrument.model.EstadoConfiguracion.PUBLICADO)
            """)
    List<OpcionPuntajeDimension> findActiveOfficialByVersionAndOptionIds(Long versionTestId,
            Collection<Long> optionIds);
}
