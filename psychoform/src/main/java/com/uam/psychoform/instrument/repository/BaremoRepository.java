package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.entity.Baremo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BaremoRepository extends JpaRepository<Baremo, Long> {
    @Query("""
            select b from Baremo b
            where b.versionTest.id = :versionTestId
              and b.dimensionResultado.id = :dimensionId
              and b.estado in (com.uam.psychoform.instrument.entity.EstadoConfiguracion.APROBADO,
                               com.uam.psychoform.instrument.entity.EstadoConfiguracion.PUBLICADO)
            order by b.aprobadoEn desc nulls last, b.id desc
            """)
    Optional<Baremo> findPreferredDimensionBaremo(Long versionTestId, Long dimensionId);
}
