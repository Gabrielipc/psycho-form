package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.model.ImagenItem;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ImagenItemRepository extends JpaRepository<ImagenItem, Long> {
    List<ImagenItem> findByItemIdOrderByNumeroOrdenAsc(Long itemId);

    @Query("""
            select image from ImagenItem image
            join fetch image.item item
            join fetch image.recurso
            where item.id in :itemIds
            order by item.numeroOrden asc, image.numeroOrden asc
            """)
    List<ImagenItem> findByItemIdInOrderByItemAndImageOrder(Collection<Long> itemIds);

    @Query("""
            select image
            from ImagenItem image
            join image.item item
            join item.subtest subtest
            join subtest.versionTest version
            where version.test.id = :testId
            order by subtest.numeroOrden asc, item.numeroOrden asc, image.numeroOrden asc
            """)
    List<ImagenItem> findByTestIdOrderByInstrument(Long testId);

    @Query("""
            select image
            from ImagenItem image
            join image.item item
            where item.subtest.id = :subtestId
            order by item.numeroOrden asc, image.numeroOrden asc
            """)
    List<ImagenItem> findBySubtestIdOrderByInstrument(Long subtestId);
}
