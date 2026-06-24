package com.uam.psychoform.assessment.service;

import com.uam.psychoform.assessment.entity.EstadoSesionAplicacion;
import com.uam.psychoform.assessment.entity.SesionAplicacion;
import com.uam.psychoform.assessment.repository.SesionAplicacionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SesionAplicacionService {
    private final SesionAplicacionRepository repository;
    public SesionAplicacionService(SesionAplicacionRepository repository) { this.repository = repository; }

    @Transactional
    public SesionAplicacion abrir(Long sesionId) { return transicionar(sesionId, EstadoSesionAplicacion.PROGRAMADA, EstadoSesionAplicacion.ABIERTA); }

    @Transactional
    public SesionAplicacion cerrar(Long sesionId) { return transicionar(sesionId, EstadoSesionAplicacion.ABIERTA, EstadoSesionAplicacion.CERRADA); }

    @Transactional
    public SesionAplicacion cancelar(Long sesionId) {
        SesionAplicacion sesion = obtenerBloqueada(sesionId);
        if (sesion.getEstado() == EstadoSesionAplicacion.CERRADA || sesion.getEstado() == EstadoSesionAplicacion.CANCELADA) {
            throw new IllegalStateException("La sesión no puede cancelarse en su estado actual");
        }
        sesion.setEstado(EstadoSesionAplicacion.CANCELADA);
        return repository.save(sesion);
    }

    private SesionAplicacion transicionar(Long id, EstadoSesionAplicacion origen, EstadoSesionAplicacion destino) {
        SesionAplicacion sesion = obtenerBloqueada(id);
        if (sesion.getEstado() != origen) throw new IllegalStateException("Transición de sesión inválida");
        sesion.setEstado(destino);
        return repository.save(sesion);
    }

    private SesionAplicacion obtenerBloqueada(Long id) {
        return repository.findByIdForUpdate(id).orElseThrow(() -> new EntityNotFoundException("Sesión no encontrada: " + id));
    }
}
