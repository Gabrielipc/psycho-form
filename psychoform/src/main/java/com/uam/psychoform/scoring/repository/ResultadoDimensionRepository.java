package com.uam.psychoform.scoring.repository;

import com.uam.psychoform.scoring.model.ResultadoDimension;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ResultadoDimensionRepository extends JpaRepository<ResultadoDimension, Long> {
    @Query("""
            select dimension from ResultadoDimension dimension
            join fetch dimension.dimensionResultado
            where dimension.resultado.id = :resultadoId
            """)
    List<ResultadoDimension> findByResultadoId(Long resultadoId);

    @Query("""
            select dimension from ResultadoDimension dimension
            join fetch dimension.dimensionResultado
            where dimension.resultado.id in :resultadoIds
            """)
    List<ResultadoDimension> findByResultadoIdIn(Collection<Long> resultadoIds);
}
