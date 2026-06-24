package com.uam.psychoform.academic.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.uam.psychoform.academic.entity.Participante;
import com.uam.psychoform.academic.repository.ParticipanteRepository;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ParticipanteServiceTest {

    @Test
    void rechazaRegistrarUnCodigoParticipanteDuplicado() {
        ParticipanteRepository repository = Mockito.mock(ParticipanteRepository.class);
        when(repository.existsByCodigoParticipante("P-001")).thenReturn(true);
        ParticipanteService service = new ParticipanteService(repository, Clock.systemUTC());

        assertThatThrownBy(() -> service.registrar("P-001", "Ana", "López"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("P-001");
    }

    @Test
    void desactivaElParticipanteSinEliminarlo() {
        ParticipanteRepository repository = Mockito.mock(ParticipanteRepository.class);
        Participante participante = new Participante();
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(participante));
        ParticipanteService service = new ParticipanteService(repository, Clock.systemUTC());

        service.desactivar(id);

        org.assertj.core.api.Assertions.assertThat(participante.getEstado())
                .isEqualTo(com.uam.psychoform.security.entity.EstadoGeneral.INACTIVO);
    }
}
