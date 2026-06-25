package com.uam.psychoform.assessment.repository;

import com.uam.psychoform.assessment.entity.SesionSubtest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SesionSubtestRepository extends JpaRepository<SesionSubtest, Long> {
    Optional<SesionSubtest> findBySesionAplicacionIdAndSubtestId(Long sesionAplicacionId, Long subtestId);

    List<SesionSubtest> findBySesionAplicacionIdOrderByNumeroOrdenAsc(Long sesionAplicacionId);
}
