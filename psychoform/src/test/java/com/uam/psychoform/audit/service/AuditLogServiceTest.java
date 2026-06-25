package com.uam.psychoform.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uam.psychoform.audit.model.Auditoria;
import com.uam.psychoform.audit.repository.AuditoriaRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.prepost.PreAuthorize;

class AuditLogServiceTest {
    private final AuditoriaRepository repository = Mockito.mock(AuditoriaRepository.class);
    private final UsuarioRepository usuarios = Mockito.mock(UsuarioRepository.class);
    private final CurrentActor currentActor = Mockito.mock(CurrentActor.class);
    private final AuditLogService service = new AuditLogService(repository, usuarios, currentActor,
            Clock.fixed(Instant.parse("2026-06-24T12:00:00Z"), ZoneOffset.UTC));

    @Test
    void recordUsaActorActualCuandoExiste() {
        UUID actorId = UUID.randomUUID();
        Usuario actor = new Usuario();
        actor.setId(actorId);
        when(currentActor.usuarioId()).thenReturn(actorId);
        when(usuarios.findById(actorId)).thenReturn(Optional.of(actor));

        Auditoria audit = service.record(new AuditLogService.AuditEvent("ATTEMPT_SCORED", "intento_test", "7",
                "{}", "{\"estado\":\"CALCULADO\"}", "127.0.0.1", "JUnit"));

        assertThat(audit.getUsuario()).isSameAs(actor);
        assertThat(audit.getAccion()).isEqualTo("ATTEMPT_SCORED");
        verify(repository).save(audit);
    }

    @Test
    void listByEntityRequierePermisoAuditoriaVer() throws Exception {
        Method method = AuditLogService.class.getMethod("listByEntity", String.class, String.class);

        assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAuthority('PERM_AUDITORIA_VER')");
    }
}
