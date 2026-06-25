package com.uam.psychoform.scoring.repository;

import com.uam.psychoform.scoring.model.Resultado;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ResultadoRepository extends JpaRepository<Resultado, Long> {
    Optional<Resultado> findByIntentoId(Long intentoId);

    @Query("""
            select r from Resultado r
            join fetch r.intento i
            join fetch i.asignacion a
            where a.sesionAplicacion.id = :sessionId
            """)
    List<Resultado> findBySessionId(Long sessionId);
}
