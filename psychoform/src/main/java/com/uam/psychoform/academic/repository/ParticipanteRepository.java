package com.uam.psychoform.academic.repository;

import com.uam.psychoform.academic.model.Participante;
import com.uam.psychoform.security.model.EstadoGeneral;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ParticipanteRepository extends JpaRepository<Participante, UUID> {

    boolean existsByCodigoParticipante(String codigoParticipante);

    List<Participante> findAllByEstado(EstadoGeneral estado);

    @Query("""
            select p from Participante p
            where lower(p.codigoParticipante) like concat('%', lower(:term), '%')
               or lower(p.nombres) like concat('%', lower(:term), '%')
               or lower(p.apellidos) like concat('%', lower(:term), '%')
            order by p.codigoParticipante asc, p.apellidos asc, p.nombres asc
            """)
    List<Participante> searchByTerm(String term, Pageable pageable);
}
