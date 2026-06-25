package com.uam.psychoform.instrument.service;

import com.uam.psychoform.instrument.entity.EstadoVersionTest;
import com.uam.psychoform.instrument.entity.VersionTest;
import com.uam.psychoform.instrument.repository.VersionTestRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional(readOnly = true)
public class VersionTestService {
    private final VersionTestRepository repository;

    public VersionTestService(VersionTestRepository repository) { this.repository = repository; }

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
}
