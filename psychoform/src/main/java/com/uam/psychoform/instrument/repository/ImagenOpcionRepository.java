package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.model.ImagenOpcion;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ImagenOpcionRepository extends JpaRepository<ImagenOpcion, Long> {
    List<ImagenOpcion> findByOpcionIdOrderByNumeroOrdenAsc(Long opcionId);

    @Query("""
            select image from ImagenOpcion image
            join fetch image.opcion opcion
            join fetch image.recurso
            where opcion.id in :opcionIds
            order by opcion.numeroOrden asc, image.numeroOrden asc
            """)
    List<ImagenOpcion> findByOpcionIdInOrderByOptionAndImageOrder(Collection<Long> opcionIds);
}
