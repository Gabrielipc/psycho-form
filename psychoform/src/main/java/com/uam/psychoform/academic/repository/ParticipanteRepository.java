package com.uam.psychoform.academic.repository;

import com.uam.psychoform.academic.model.Participante;
import com.uam.psychoform.security.model.EstadoGeneral;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipanteRepository extends JpaRepository<Participante, UUID> {

    boolean existsByCodigoParticipante(String codigoParticipante);

    List<Participante> findAllByEstado(EstadoGeneral estado);
}
