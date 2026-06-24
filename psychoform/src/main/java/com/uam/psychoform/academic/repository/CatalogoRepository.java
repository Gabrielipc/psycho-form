package com.uam.psychoform.academic.repository;

import com.uam.psychoform.academic.entity.CatalogoEntidad;
import com.uam.psychoform.security.entity.EstadoGeneral;
import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.jpa.repository.JpaRepository;

@NoRepositoryBean
public interface CatalogoRepository<T extends CatalogoEntidad, ID> extends JpaRepository<T, ID> {

    List<T> findAllByEstado(EstadoGeneral estado);

    default List<T> buscarActivos(String termino) {
        return buscarPorEstadoYTermino(EstadoGeneral.ACTIVO, termino);
    }

    List<T> buscarPorEstadoYTermino(EstadoGeneral estado, String termino);
}
