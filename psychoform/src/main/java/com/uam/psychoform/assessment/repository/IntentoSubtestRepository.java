package com.uam.psychoform.assessment.repository;

import com.uam.psychoform.assessment.entity.IntentoSubtest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntentoSubtestRepository extends JpaRepository<IntentoSubtest, Long> {
    Optional<IntentoSubtest> findByIntentoIdAndSubtestId(Long intentoId, Long subtestId);

    List<IntentoSubtest> findByIntentoId(Long intentoId);
}
