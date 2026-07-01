package com.uam.psychoform.audit.repository;

import com.uam.psychoform.audit.model.Auditoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findByEntidadAndEntidadIdOrderByCreadoEnDesc(String entidad, String entidadId);
    List<Auditoria> findAllByOrderByCreadoEnDesc();
}
