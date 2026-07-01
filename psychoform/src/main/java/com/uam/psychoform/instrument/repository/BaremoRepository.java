package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.model.Baremo;
import com.uam.psychoform.instrument.model.EstadoConfiguracion;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BaremoRepository extends JpaRepository<Baremo, Long> {
    default Optional<Baremo> findPreferredDimensionBaremo(Long versionTestId, Long dimensionId) {
        return findPreferredDimensionBaremoByEstados(versionTestId, dimensionId,
                List.of(EstadoConfiguracion.APROBADO, EstadoConfiguracion.PUBLICADO));
    }

    default Optional<Baremo> findPreferredTotalBaremo(Long versionTestId) {
        return findPreferredTotalBaremoByEstados(versionTestId,
                List.of(EstadoConfiguracion.APROBADO, EstadoConfiguracion.PUBLICADO));
    }

    @Query("""
            select b from Baremo b
            where b.versionTest.id = :versionTestId
              and b.dimensionResultado is null
              and b.estado in :estados
            order by b.aprobadoEn desc nulls last, b.id desc
            """)
    Optional<Baremo> findPreferredTotalBaremoByEstados(Long versionTestId, Collection<EstadoConfiguracion> estados);

    @Query("""
            select b from Baremo b
            where b.versionTest.id = :versionTestId
              and b.dimensionResultado.id = :dimensionId
              and b.estado in :estados
            order by b.aprobadoEn desc nulls last, b.id desc
            """)
    Optional<Baremo> findPreferredDimensionBaremoByEstados(Long versionTestId, Long dimensionId,
            Collection<EstadoConfiguracion> estados);

    default List<Baremo> findPreferredDimensionBaremos(Long versionTestId, Collection<Long> dimensionIds) {
        return findPreferredDimensionBaremosByEstados(versionTestId, dimensionIds,
                List.of(EstadoConfiguracion.APROBADO, EstadoConfiguracion.PUBLICADO));
    }

    @Query("""
            select b from Baremo b
            join fetch b.dimensionResultado
            where b.versionTest.id = :versionTestId
              and b.dimensionResultado.id in :dimensionIds
              and b.estado in :estados
            order by b.dimensionResultado.id asc, b.aprobadoEn desc nulls last, b.id desc
            """)
    List<Baremo> findPreferredDimensionBaremosByEstados(Long versionTestId, Collection<Long> dimensionIds,
            Collection<EstadoConfiguracion> estados);
}
