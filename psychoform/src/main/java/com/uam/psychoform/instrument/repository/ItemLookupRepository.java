package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.model.Item;
import com.uam.psychoform.security.model.EstadoGeneral;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItemLookupRepository extends JpaRepository<Item, Long> {
    List<Item> findBySubtestIdAndEstadoOrderByNumeroOrdenAsc(Long subtestId, EstadoGeneral estado);

    @Query("""
            select item from Item item
            where item.subtest.id in :subtestIds
              and item.estado = :estado
            order by item.subtest.numeroOrden asc, item.numeroOrden asc
            """)
    List<Item> findBySubtestIdInAndEstadoOrderBySubtestAndItemOrder(Collection<Long> subtestIds,
            EstadoGeneral estado);
}
