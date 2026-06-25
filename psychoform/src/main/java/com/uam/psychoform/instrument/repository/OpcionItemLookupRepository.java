package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.model.OpcionItem;
import com.uam.psychoform.security.model.EstadoGeneral;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpcionItemLookupRepository extends JpaRepository<OpcionItem, Long> {
    List<OpcionItem> findByItemIdAndEstadoOrderByNumeroOrdenAsc(Long itemId, EstadoGeneral estado);
}
