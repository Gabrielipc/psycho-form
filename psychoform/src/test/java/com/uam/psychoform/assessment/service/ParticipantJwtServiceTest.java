package com.uam.psychoform.assessment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uam.psychoform.security.service.JwtService;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class ParticipantJwtServiceTest {
    private final JwtService jwt = Mockito.mock(JwtService.class);
    private final ParticipantJwtService service = new ParticipantJwtService(jwt);

    @Test
    void issueNoIncluyePermisoInternoDeRegistroDeRespuestas() {
        UUID participantId = UUID.randomUUID();
        when(jwt.emitir(Mockito.eq(participantId), Mockito.eq("participant-assignment:77"), Mockito.anySet(),
                Mockito.eq(Set.of("PARTICIPANTE")))).thenReturn("token");

        assertThat(service.issue(participantId, 77L)).isEqualTo("token");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> permissions = ArgumentCaptor.forClass(Set.class);
        verify(jwt).emitir(Mockito.eq(participantId), Mockito.eq("participant-assignment:77"), permissions.capture(),
                Mockito.eq(Set.of("PARTICIPANTE")));
        assertThat(permissions.getValue()).isEmpty();
    }

    @Test
    void requireAccessExtraeAsignacionYParticipanteDelBearer() {
        UUID participantId = UUID.randomUUID();
        when(jwt.validar("token")).thenReturn(new JwtService.JwtPrincipal(participantId,
                "participant-assignment:77", Set.of(), Set.of("PARTICIPANTE"), 1L));

        ParticipantAccessService.ParticipantAccess access = service.requireAccess("Bearer token");

        assertThat(access.assignmentId()).isEqualTo(77L);
        assertThat(access.participantId()).isEqualTo(participantId);
    }
}
