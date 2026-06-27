package com.uam.psychoform.academic.service;

import com.uam.psychoform.academic.model.Participante;
import com.uam.psychoform.academic.repository.CarreraRepository;
import com.uam.psychoform.academic.repository.CatalogoSexoRepository;
import com.uam.psychoform.academic.repository.CohorteRepository;
import com.uam.psychoform.academic.repository.GrupoAcademicoRepository;
import com.uam.psychoform.academic.repository.ParticipanteRepository;
import com.uam.psychoform.security.SecurityPermissions;
import com.uam.psychoform.security.model.EstadoGeneral;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional(readOnly = true)
public class ParticipanteService {

    private final ParticipanteRepository repository;
    private final CatalogoSexoRepository sexoRepository;
    private final CarreraRepository carreraRepository;
    private final CohorteRepository cohorteRepository;
    private final GrupoAcademicoRepository grupoRepository;
    private final Clock clock;

    public ParticipanteService(
            ParticipanteRepository repository,
            CatalogoSexoRepository sexoRepository,
            CarreraRepository carreraRepository,
            CohorteRepository cohorteRepository,
            GrupoAcademicoRepository grupoRepository,
            Clock clock) {
        this.repository = repository;
        this.sexoRepository = sexoRepository;
        this.carreraRepository = carreraRepository;
        this.cohorteRepository = cohorteRepository;
        this.grupoRepository = grupoRepository;
        this.clock = clock;
    }

    @PreAuthorize(SecurityPermissions.PARTICIPANTE_LEER)
    public List<Participante> listar() {
        return repository.findAll();
    }

    @PreAuthorize(SecurityPermissions.PARTICIPANTE_LEER)
    public Participante obtener(UUID participanteId) {
        return repository.findById(participanteId)
                .orElseThrow(() -> new EntityNotFoundException("Participante no encontrado: " + participanteId));
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.PARTICIPANTE_CREAR)
    public Participante registrar(String codigoParticipante, String nombres, String apellidos) {
        return registrar(codigoParticipante, nombres, apellidos, null, null, null, null);
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.PARTICIPANTE_CREAR)
    public Participante registrar(
            String codigoParticipante,
            String nombres,
            String apellidos,
            Short sexoId,
            Short carreraId,
            Short cohorteId,
            Short grupoAcademicoId) {
        if (repository.existsByCodigoParticipante(codigoParticipante)) {
            throw new IllegalStateException("Ya existe el participante: " + codigoParticipante);
        }
        LocalDateTime ahora = LocalDateTime.now(clock);
        Participante participante = new Participante();
        participante.setId(UUID.randomUUID());
        participante.setCodigoParticipante(codigoParticipante);
        participante.setNombres(nombres);
        participante.setApellidos(apellidos);
        participante.setEstado(EstadoGeneral.ACTIVO);
        participante.setCreadoEn(ahora);
        participante.setActualizadoEn(ahora);
        if (sexoId != null) {
            participante.setSexo(sexoRepository.getReferenceById(sexoId));
        }
        if (carreraId != null) {
            participante.setCarrera(carreraRepository.getReferenceById(carreraId));
        }
        if (cohorteId != null) {
            participante.setCohorte(cohorteRepository.getReferenceById(cohorteId));
        }
        if (grupoAcademicoId != null) {
            participante.setGrupoAcademico(grupoRepository.getReferenceById(grupoAcademicoId));
        }
        return repository.save(participante);
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.PARTICIPANTE_ELIMINAR)
    public void desactivar(UUID participanteId) {
        Participante participante = obtener(participanteId);
        participante.setEstado(EstadoGeneral.INACTIVO);
        participante.setActualizadoEn(LocalDateTime.now(clock));
        repository.save(participante);
    }
}
