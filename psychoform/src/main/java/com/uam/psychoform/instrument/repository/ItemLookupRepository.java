package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.model.Item;
import com.uam.psychoform.security.model.EstadoGeneral;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemLookupRepository extends JpaRepository<Item, Long> {
    List<Item> findBySubtestIdAndEstadoOrderByNumeroOrdenAsc(Long subtestId, EstadoGeneral estado);
}
