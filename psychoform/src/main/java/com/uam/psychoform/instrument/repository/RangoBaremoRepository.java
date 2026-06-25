package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.model.RangoBaremo;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RangoBaremoRepository extends JpaRepository<RangoBaremo, Long> {
    @Query("""
            select r from RangoBaremo r
            where r.baremo.id = :baremoId
              and :score between r.puntajeMinimo and r.puntajeMaximo
            order by r.orden asc, r.id asc
            """)
    Optional<RangoBaremo> findMatchingRange(Long baremoId, BigDecimal score);
}
