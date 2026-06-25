package com.uam.psychoform.academic.repository;

import com.uam.psychoform.academic.model.Cohorte;
import com.uam.psychoform.security.model.EstadoGeneral;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CohorteRepository extends CatalogoRepository<Cohorte, Short> {

    @Override
    @Query("""
            select catalogo
            from Cohorte catalogo
            where catalogo.estado = :estado
              and (lower(catalogo.codigoCohorte) like lower(concat('%', :termino, '%'))
                or lower(catalogo.nombreCohorte) like lower(concat('%', :termino, '%')))
            """)
    List<Cohorte> buscarPorEstadoYTermino(
            @Param("estado") EstadoGeneral estado,
            @Param("termino") String termino);
}
