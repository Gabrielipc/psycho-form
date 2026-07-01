package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.model.Baremo;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BaremoRepository extends JpaRepository<Baremo, Long> {
    @Query("""
            select b from Baremo b
            where b.versionTest.id = :versionTestId
              and b.dimensionResultado.id = :dimensionId
              and b.estado in (com.uam.psychoform.instrument.model.EstadoConfiguracion.APROBADO,
                               com.uam.psychoform.instrument.model.EstadoConfiguracion.PUBLICADO)
            order by b.aprobadoEn desc nulls last, b.id desc
            """)
    Optional<Baremo> findPreferredDimensionBaremo(Long versionTestId, Long dimensionId);

    @Query("""
            select b from Baremo b
            join fetch b.dimensionResultado
            where b.versionTest.id = :versionTestId
              and b.dimensionResultado.id in :dimensionIds
              and b.estado in (com.uam.psychoform.instrument.model.EstadoConfiguracion.APROBADO,
                               com.uam.psychoform.instrument.model.EstadoConfiguracion.PUBLICADO)
            order by b.dimensionResultado.id asc, b.aprobadoEn desc nulls last, b.id desc
            """)
    List<Baremo> findPreferredDimensionBaremos(Long versionTestId, Collection<Long> dimensionIds);
}
