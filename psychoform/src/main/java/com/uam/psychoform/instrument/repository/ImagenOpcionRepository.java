package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.model.ImagenOpcion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImagenOpcionRepository extends JpaRepository<ImagenOpcion, Long> {
    List<ImagenOpcion> findByOpcionIdOrderByNumeroOrdenAsc(Long opcionId);
}
