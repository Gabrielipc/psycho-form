package com.uam.psychoform.academic.repository;

import com.uam.psychoform.academic.model.CatalogoSexo;
import com.uam.psychoform.security.model.EstadoGeneral;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CatalogoSexoRepository extends CatalogoRepository<CatalogoSexo, Short> {

    @Override
    @Query("""
            select catalogo
            from CatalogoSexo catalogo
            where catalogo.estado = :estado
              and (lower(catalogo.codigo) like lower(concat('%', :termino, '%'))
                or lower(catalogo.nombre) like lower(concat('%', :termino, '%')))
            """)
    List<CatalogoSexo> buscarPorEstadoYTermino(
            @Param("estado") EstadoGeneral estado,
            @Param("termino") String termino);
}
