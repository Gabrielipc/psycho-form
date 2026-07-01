package com.uam.psychoform.instrument.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.uam.psychoform.instrument.model.EstadoVersionTest;
import com.uam.psychoform.instrument.model.EstrategiaCalificacion;
import com.uam.psychoform.instrument.model.VersionTest;
import com.uam.psychoform.instrument.repository.BaremoPublicationRepository;
import com.uam.psychoform.instrument.repository.ReglaCalificacionPublicationRepository;
import com.uam.psychoform.instrument.repository.VersionTestRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.InOrder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

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

    @Test
    void publicarVersionPublicaReglasYBaremosActivosEnLaMismaTransaccion() throws Exception {
        VersionTestRepository repository = Mockito.mock(VersionTestRepository.class);
        UsuarioRepository usuarios = Mockito.mock(UsuarioRepository.class);
        CurrentActor currentActor = Mockito.mock(CurrentActor.class);
        ReglaCalificacionPublicationRepository reglas = Mockito.mock(ReglaCalificacionPublicationRepository.class);
        BaremoPublicationRepository baremos = Mockito.mock(BaremoPublicationRepository.class);
        UUID actorId = UUID.randomUUID();
        Usuario actor = new Usuario();
        actor.setId(actorId);
        Clock clock = Clock.fixed(Instant.parse("2026-07-01T20:30:00Z"), ZoneOffset.UTC);
        LocalDateTime approvedAt = LocalDateTime.now(clock);
        VersionTest version = new VersionTest();
        version.setId(11L);
        version.setEstado(EstadoVersionTest.APROBADO);
        version.setEstrategiaCalificacion(new EstrategiaCalificacion());
        when(repository.findByIdForUpdate(11L)).thenReturn(Optional.of(version));
        when(currentActor.usuarioId()).thenReturn(actorId);
        when(usuarios.findById(actorId)).thenReturn(Optional.of(actor));
        when(reglas.publishActiveByVersionId(11L, actor, approvedAt)).thenReturn(3);
        when(baremos.publishActiveByVersionId(11L, actor, approvedAt)).thenReturn(2);
        VersionTestService service = new VersionTestService(repository, usuarios, currentActor, clock,
                reglas, baremos);

        Method method = VersionTestService.class.getMethod("publishVersion", long.class);
        VersionTest published = service.publishVersion(11L);

        assertThat(method.getAnnotation(Transactional.class)).isNotNull();
        assertThat(published.getEstado()).isEqualTo(EstadoVersionTest.PUBLICADO);
        InOrder inOrder = Mockito.inOrder(reglas, baremos, repository);
        inOrder.verify(reglas).publishActiveByVersionId(11L, actor, approvedAt);
        inOrder.verify(baremos).publishActiveByVersionId(11L, actor, approvedAt);
        inOrder.verify(repository).save(version);
    }

    @Test
    void publicarVersionRechazaConfiguracionSinReglasActivas() {
        VersionTestRepository repository = Mockito.mock(VersionTestRepository.class);
        UsuarioRepository usuarios = Mockito.mock(UsuarioRepository.class);
        CurrentActor currentActor = Mockito.mock(CurrentActor.class);
        ReglaCalificacionPublicationRepository reglas = Mockito.mock(ReglaCalificacionPublicationRepository.class);
        BaremoPublicationRepository baremos = Mockito.mock(BaremoPublicationRepository.class);
        UUID actorId = UUID.randomUUID();
        Usuario actor = new Usuario();
        actor.setId(actorId);
        VersionTest version = new VersionTest();
        version.setId(12L);
        version.setEstado(EstadoVersionTest.APROBADO);
        version.setEstrategiaCalificacion(new EstrategiaCalificacion());
        when(repository.findByIdForUpdate(12L)).thenReturn(Optional.of(version));
        when(currentActor.usuarioId()).thenReturn(actorId);
        when(usuarios.findById(actorId)).thenReturn(Optional.of(actor));
        when(reglas.publishActiveByVersionId(Mockito.eq(12L), Mockito.eq(actor), Mockito.any(LocalDateTime.class)))
                .thenReturn(0);
        VersionTestService service = new VersionTestService(repository, usuarios, currentActor, java.time.Clock.systemUTC(),
                reglas, baremos);

        assertThatThrownBy(() -> service.publishVersion(12L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reglas de calificación activas");

        assertThat(version.getEstado()).isEqualTo(EstadoVersionTest.APROBADO);
        Mockito.verifyNoInteractions(baremos);
        Mockito.verify(repository, Mockito.never()).save(version);
    }
}
