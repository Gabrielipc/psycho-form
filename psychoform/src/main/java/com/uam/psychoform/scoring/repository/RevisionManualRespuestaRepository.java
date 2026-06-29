package com.uam.psychoform.scoring.repository;

import com.uam.psychoform.scoring.model.EstadoRevisionManual;
import com.uam.psychoform.scoring.model.RevisionManualRespuesta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevisionManualRespuestaRepository extends JpaRepository<RevisionManualRespuesta, Long> {
    List<RevisionManualRespuesta> findByEstadoOrderByCreadoEnAsc(EstadoRevisionManual estado);
}
