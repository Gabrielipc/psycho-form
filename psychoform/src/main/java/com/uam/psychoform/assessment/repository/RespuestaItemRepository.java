package com.uam.psychoform.assessment.repository;

import com.uam.psychoform.assessment.model.RespuestaItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RespuestaItemRepository extends JpaRepository<RespuestaItem, Long> {
    List<RespuestaItem> findByIntentoId(Long intentoId);

    @Query("""
            select r from RespuestaItem r
            join fetch r.item
            where r.intento.id = :intentoId
            """)
    List<RespuestaItem> findByIntentoIdWithItem(Long intentoId);

    Optional<RespuestaItem> findByIntentoIdAndItemId(Long intentoId, Long itemId);
}
