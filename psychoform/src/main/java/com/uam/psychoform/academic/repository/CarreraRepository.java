package com.uam.psychoform.academic.repository;

import com.uam.psychoform.academic.model.Carrera;
import com.uam.psychoform.security.model.EstadoGeneral;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarreraRepository extends CatalogoRepository<Carrera, Short> {

    @Override
    @Query("""
            select catalogo
            from Carrera catalogo
            where catalogo.estado = :estado
              and (lower(catalogo.codigoCarrera) like lower(concat('%', :termino, '%'))
                or lower(catalogo.nombreCarrera) like lower(concat('%', :termino, '%')))
            """)
    List<Carrera> buscarPorEstadoYTermino(
            @Param("estado") EstadoGeneral estado,
            @Param("termino") String termino);
}
