package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.model.EstadoConfiguracion;
import com.uam.psychoform.instrument.model.ReglaCalificacion;
import com.uam.psychoform.security.model.Usuario;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ReglaCalificacionPublicationRepository extends JpaRepository<ReglaCalificacion, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ReglaCalificacion r
               set r.estado = :published,
                   r.aprobadoPor = :approvedBy,
                   r.aprobadoEn = :approvedAt
             where r.versionTest.id = :versionId
               and r.activa = true
               and r.estado <> :retired
            """)
    int publishActiveByVersionId(Long versionId, EstadoConfiguracion published, EstadoConfiguracion retired,
            Usuario approvedBy, LocalDateTime approvedAt);

    default int publishActiveByVersionId(Long versionId, Usuario approvedBy, LocalDateTime approvedAt) {
        return publishActiveByVersionId(versionId, EstadoConfiguracion.PUBLICADO, EstadoConfiguracion.RETIRADO,
                approvedBy, approvedAt);
    }
}
