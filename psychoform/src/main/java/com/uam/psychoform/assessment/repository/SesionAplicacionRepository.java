package com.uam.psychoform.assessment.repository;

import com.uam.psychoform.assessment.model.SesionAplicacion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

public interface SesionAplicacionRepository extends JpaRepository<SesionAplicacion, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SesionAplicacion s where s.id = :id")
    Optional<SesionAplicacion> findByIdForUpdate(Long id);

    @Query("""
            select s from SesionAplicacion s
            where lower(s.codigoSesion) like concat('%', lower(:term), '%')
               or lower(s.nombreSesion) like concat('%', lower(:term), '%')
            order by s.codigoSesion asc, s.nombreSesion asc
            """)
    List<SesionAplicacion> searchByTerm(String term, Pageable pageable);
}
