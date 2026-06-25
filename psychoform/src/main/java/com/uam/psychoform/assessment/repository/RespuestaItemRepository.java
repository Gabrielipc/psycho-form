package com.uam.psychoform.assessment.repository;

import com.uam.psychoform.assessment.entity.RespuestaItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RespuestaItemRepository extends JpaRepository<RespuestaItem, Long> {
    List<RespuestaItem> findByIntentoId(Long intentoId);

    Optional<RespuestaItem> findByIntentoIdAndItemId(Long intentoId, Long itemId);
}
