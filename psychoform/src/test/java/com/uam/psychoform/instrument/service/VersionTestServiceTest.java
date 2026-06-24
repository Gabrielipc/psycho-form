package com.uam.psychoform.instrument.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.uam.psychoform.instrument.entity.EstadoVersionTest;
import com.uam.psychoform.instrument.entity.VersionTest;
import com.uam.psychoform.instrument.repository.VersionTestRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class VersionTestServiceTest {
    @Test
    void rechazaModificarUnaVersionQueNoEstaEnBorrador() {
        VersionTestRepository repository = Mockito.mock(VersionTestRepository.class);
        VersionTest version = new VersionTest();
        version.setEstado(EstadoVersionTest.APROBADO);
        when(repository.findByIdForUpdate(5L)).thenReturn(Optional.of(version));
        VersionTestService service = new VersionTestService(repository);

        assertThatThrownBy(() -> service.exigirBorrador(5L))
                .isInstanceOf(IllegalStateException.class);
    }
}
