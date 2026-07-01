package com.uam.psychoform.scoring.repository;

import com.uam.psychoform.scoring.model.EstadoRevisionManual;
import com.uam.psychoform.scoring.model.RevisionManualRespuesta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RevisionManualRespuestaRepository extends JpaRepository<RevisionManualRespuesta, Long> {
    List<RevisionManualRespuesta> findByEstadoOrderByCreadoEnAsc(EstadoRevisionManual estado);

    @Query("""
            select review from RevisionManualRespuesta review
            join fetch review.respuesta answer
            join fetch answer.intento attempt
            join fetch attempt.asignacion assignment
            join fetch assignment.participante
            join fetch answer.item
            where review.estado = :estado
            order by review.creadoEn asc
            """)
    List<RevisionManualRespuesta> findPendingWithAnswerContext(EstadoRevisionManual estado);
}
