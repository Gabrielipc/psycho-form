package com.uam.psychoform.assessment.repository;

import com.uam.psychoform.assessment.model.IntentoTest;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface IntentoTestRepository extends JpaRepository<IntentoTest, Long> {
    Optional<IntentoTest> findByAsignacionId(Long asignacionId);

    List<IntentoTest> findByAsignacionSesionAplicacionId(Long sesionAplicacionId);

    @Query("""
            select i from IntentoTest i
            join fetch i.asignacion a
            left join fetch i.ultimoSubtest
            where a.sesionAplicacion.id = :sesionAplicacionId
            """)
    List<IntentoTest> findByAsignacionSesionAplicacionIdWithUltimoSubtest(Long sesionAplicacionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from IntentoTest i where i.id = :id")
    Optional<IntentoTest> findByIdForUpdate(Long id);
}
