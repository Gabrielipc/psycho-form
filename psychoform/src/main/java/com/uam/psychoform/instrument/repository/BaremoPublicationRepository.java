package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.model.Baremo;
import com.uam.psychoform.instrument.model.EstadoConfiguracion;
import com.uam.psychoform.security.model.Usuario;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BaremoPublicationRepository extends JpaRepository<Baremo, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Baremo b
               set b.estado = :published,
                   b.aprobadoPor = :approvedBy,
                   b.aprobadoEn = :approvedAt
             where b.versionTest.id = :versionId
               and b.estado <> :retired
            """)
    int publishActiveByVersionId(Long versionId, EstadoConfiguracion published, EstadoConfiguracion retired,
            Usuario approvedBy, LocalDateTime approvedAt);

    default int publishActiveByVersionId(Long versionId, Usuario approvedBy, LocalDateTime approvedAt) {
        return publishActiveByVersionId(versionId, EstadoConfiguracion.PUBLICADO, EstadoConfiguracion.RETIRADO,
                approvedBy, approvedAt);
    }
}
