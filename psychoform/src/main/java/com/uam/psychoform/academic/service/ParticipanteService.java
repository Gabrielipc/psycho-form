package com.uam.psychoform.academic.service;

import com.uam.psychoform.academic.entity.Participante;
import com.uam.psychoform.academic.repository.ParticipanteRepository;
import com.uam.psychoform.security.entity.EstadoGeneral;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ParticipanteService {

    private final ParticipanteRepository repository;
    private final Clock clock;

    public ParticipanteService(ParticipanteRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public Participante registrar(String codigoParticipante, String nombres, String apellidos) {
        if (repository.existsByCodigoParticipante(codigoParticipante)) {
            throw new IllegalStateException("Ya existe el participante: " + codigoParticipante);
        }
        LocalDateTime ahora = LocalDateTime.now(clock);
        Participante participante = new Participante();
        participante.setCodigoParticipante(codigoParticipante);
        participante.setNombres(nombres);
        participante.setApellidos(apellidos);
        participante.setEstado(EstadoGeneral.ACTIVO);
        participante.setCreadoEn(ahora);
        participante.setActualizadoEn(ahora);
        return repository.save(participante);
    }

    @Transactional
    public void desactivar(UUID participanteId) {
        Participante participante = repository.findById(participanteId)
                .orElseThrow(() -> new EntityNotFoundException("Participante no encontrado: " + participanteId));
        participante.setEstado(EstadoGeneral.INACTIVO);
        participante.setActualizadoEn(LocalDateTime.now(clock));
        repository.save(participante);
    }
}
