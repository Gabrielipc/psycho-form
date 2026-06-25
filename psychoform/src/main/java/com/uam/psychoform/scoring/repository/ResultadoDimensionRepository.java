package com.uam.psychoform.scoring.repository;

import com.uam.psychoform.scoring.entity.ResultadoDimension;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultadoDimensionRepository extends JpaRepository<ResultadoDimension, Long> {
    List<ResultadoDimension> findByResultadoId(Long resultadoId);
}
