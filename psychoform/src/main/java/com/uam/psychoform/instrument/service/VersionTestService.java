package com.uam.psychoform.instrument.service;

import com.uam.psychoform.instrument.model.EstadoVersionTest;
import com.uam.psychoform.instrument.model.VersionTest;
import com.uam.psychoform.instrument.repository.VersionTestRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@Transactional(readOnly = true)
public class VersionTestService {
    private final VersionTestRepository repository;
    private final UsuarioRepository usuarios;
    private final CurrentActor currentActor;
    private final Clock clock;

    public VersionTestService(VersionTestRepository repository) {
        this(repository, null, null, Clock.systemUTC());
    }

    @Autowired
    public VersionTestService(VersionTestRepository repository, UsuarioRepository usuarios, CurrentActor currentActor,
            Clock clock) {
        this.repository = repository;
        this.usuarios = usuarios;
        this.currentActor = currentActor;
        this.clock = clock;
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_TEST_CREAR')")
    public VersionTest exigirBorrador(Long versionId) {
        VersionTest version = repository.findByIdForUpdate(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Versión no encontrada: " + versionId));
        if (version.getEstado() != EstadoVersionTest.BORRADOR) {
            throw new IllegalStateException("La versión no puede modificarse fuera de BORRADOR");
        }
        return version;
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_TEST_PUBLICAR')")
    public VersionTest approveVersion(long versionId) {
        VersionTest version = repository.findByIdForUpdate(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Versión no encontrada: " + versionId));
        if (version.getEstado() != EstadoVersionTest.BORRADOR && version.getEstado() != EstadoVersionTest.EN_REVISION) {
            throw new IllegalStateException("La versión no puede aprobarse en su estado actual");
        }
        UUID actorId = currentActor.usuarioId();
        Usuario actor = usuarios.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado: " + actorId));
        version.setEstado(EstadoVersionTest.APROBADO);
        version.setAprobadoPor(actor);
        version.setAprobadoEn(LocalDateTime.now(clock));
        repository.save(version);
        return version;
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_TEST_PUBLICAR')")
    public VersionTest publishVersion(long versionId) {
        VersionTest version = repository.findByIdForUpdate(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Versión no encontrada: " + versionId));
        if (version.getEstado() != EstadoVersionTest.APROBADO) {
            throw new IllegalStateException("La versión debe estar aprobada antes de publicarse");
        }
        if (version.getEstrategiaCalificacion() == null) {
            throw new IllegalStateException("La versión requiere estrategia de calificación para publicarse");
        }
        version.setEstado(EstadoVersionTest.PUBLICADO);
        version.setPublicadoEn(LocalDateTime.now(clock));
        repository.save(version);
        return version;
    }
}
