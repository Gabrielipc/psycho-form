package com.uam.psychoform.assessment.repository;

import com.uam.psychoform.assessment.entity.AsignacionTest;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface AsignacionTestRepository extends JpaRepository<AsignacionTest, Long> {
    Optional<AsignacionTest> findBySesionAplicacionIdAndParticipanteId(Long sesionId, UUID participanteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AsignacionTest a join fetch a.sesionAplicacion where a.id = :id")
    Optional<AsignacionTest> findByIdForUpdate(Long id);

    List<AsignacionTest> findBySesionAplicacionId(Long sesionId);
}
