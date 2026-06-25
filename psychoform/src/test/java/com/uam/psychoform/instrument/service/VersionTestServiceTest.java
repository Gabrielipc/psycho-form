package com.uam.psychoform.instrument.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.uam.psychoform.instrument.model.EstadoVersionTest;
import com.uam.psychoform.instrument.model.VersionTest;
import com.uam.psychoform.instrument.repository.VersionTestRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.prepost.PreAuthorize;

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

    @Test
    void aprobarVersionRequierePermisoTestPublicarYUsaActorActual() throws Exception {
        VersionTestRepository repository = Mockito.mock(VersionTestRepository.class);
        UsuarioRepository usuarios = Mockito.mock(UsuarioRepository.class);
        CurrentActor currentActor = Mockito.mock(CurrentActor.class);
        UUID actorId = UUID.randomUUID();
        Usuario actor = new Usuario();
        actor.setId(actorId);
        VersionTest version = new VersionTest();
        version.setEstado(EstadoVersionTest.EN_REVISION);
        when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(version));
        when(currentActor.usuarioId()).thenReturn(actorId);
        when(usuarios.findById(actorId)).thenReturn(Optional.of(actor));
        VersionTestService service = new VersionTestService(repository, usuarios, currentActor, java.time.Clock.systemUTC());

        Method method = VersionTestService.class.getMethod("approveVersion", long.class);

        assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAuthority('PERM_TEST_PUBLICAR')");
        assertThat(service.approveVersion(7L).getAprobadoPor()).isSameAs(actor);
        assertThat(version.getEstado()).isEqualTo(EstadoVersionTest.APROBADO);
    }
}
