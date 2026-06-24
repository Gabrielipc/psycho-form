package com.uam.psychoform.academic.repository;

import com.uam.psychoform.academic.entity.GrupoAcademico;
import com.uam.psychoform.security.entity.EstadoGeneral;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GrupoAcademicoRepository extends CatalogoRepository<GrupoAcademico, Short> {

    @Override
    @Query("""
            select catalogo
            from GrupoAcademico catalogo
            where catalogo.estado = :estado
              and (lower(catalogo.codigoGrupo) like lower(concat('%', :termino, '%'))
                or lower(catalogo.nombreGrupo) like lower(concat('%', :termino, '%')))
            """)
    List<GrupoAcademico> buscarPorEstadoYTermino(
            @Param("estado") EstadoGeneral estado,
            @Param("termino") String termino);
}
