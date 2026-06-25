package com.uam.psychoform.academic.service;

import com.uam.psychoform.academic.model.CatalogoEntidad;
import com.uam.psychoform.academic.repository.CatalogoRepository;
import com.uam.psychoform.security.model.EstadoGeneral;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public abstract class CatalogoService<T extends CatalogoEntidad, ID> {

    private final CatalogoRepository<T, ID> repository;

    protected CatalogoService(CatalogoRepository<T, ID> repository) {
        this.repository = repository;
    }

    public List<T> listarActivos() {
        return repository.findAllByEstado(EstadoGeneral.ACTIVO);
    }

    public List<T> buscarActivos(String termino) {
        return repository.buscarActivos(termino);
    }

    public T obtenerPorId(ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catálogo no encontrado: " + id));
    }

    @Transactional
    public T guardar(T entidad) {
        return repository.save(entidad);
    }

    @Transactional
    public void eliminarPorId(ID id) {
        T entidad = obtenerPorId(id);
        entidad.setEstado(EstadoGeneral.INACTIVO);
        repository.save(entidad);
    }
}
