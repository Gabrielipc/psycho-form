package com.uam.psychoform.assessment.repository;

import com.uam.psychoform.assessment.model.IntentoSubtest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IntentoSubtestRepository extends JpaRepository<IntentoSubtest, Long> {
    Optional<IntentoSubtest> findByIntentoIdAndSubtestId(Long intentoId, Long subtestId);

    List<IntentoSubtest> findByIntentoId(Long intentoId);

    @Query("""
            select i from IntentoSubtest i
            join fetch i.subtest
            where i.intento.id = :intentoId
            """)
    List<IntentoSubtest> findByIntentoIdWithSubtest(Long intentoId);

        @Query("""
            select i.intento.id as intentoId, count(i.id) as completedCount
            from IntentoSubtest i
            where i.intento.id in :intentoIds
              and i.estado = :#{T(com.uam.psychoform.assessment.model.EstadoIntento).COMPLETADO}
            group by i.intento.id
            """)
    List<CompletedSubtestCount> countCompletedByIntentoIds(Collection<Long> intentoIds);
}
