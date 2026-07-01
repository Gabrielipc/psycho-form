package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.model.OpcionItem;
import com.uam.psychoform.security.model.EstadoGeneral;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OpcionItemLookupRepository extends JpaRepository<OpcionItem, Long> {
    List<OpcionItem> findByItemIdAndEstadoOrderByNumeroOrdenAsc(Long itemId, EstadoGeneral estado);

    @Query("""
            select opcion from OpcionItem opcion
            where opcion.item.id in :itemIds
              and opcion.estado = :estado
            order by opcion.item.numeroOrden asc, opcion.numeroOrden asc
            """)
    List<OpcionItem> findByItemIdInAndEstadoOrderByItemAndOptionOrder(Collection<Long> itemIds, EstadoGeneral estado);
}
