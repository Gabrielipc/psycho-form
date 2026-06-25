package com.uam.psychoform.academic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uam.psychoform.academic.model.CatalogoEntidad;
import com.uam.psychoform.academic.repository.CatalogoRepository;
import com.uam.psychoform.security.model.EstadoGeneral;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CatalogoServiceTest {

    private CatalogoRepository<CatalogoPrueba, Short> repository;
    private CatalogoService<CatalogoPrueba, Short> service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        repository = Mockito.mock(CatalogoRepository.class);
        service = new CatalogoService<>(repository) { };
    }

    @Test
    void listaSoloLosRegistrosActivos() {
        CatalogoPrueba activo = new CatalogoPrueba(EstadoGeneral.ACTIVO);
        when(repository.findAllByEstado(EstadoGeneral.ACTIVO)).thenReturn(List.of(activo));

        assertThat(service.listarActivos()).containsExactly(activo);
    }

    @Test
    void delegaLaBusquedaAlRepositorio() {
        CatalogoPrueba activo = new CatalogoPrueba(EstadoGeneral.ACTIVO);
        when(repository.buscarActivos("mat")).thenReturn(List.of(activo));

        assertThat(service.buscarActivos("mat")).containsExactly(activo);
        verify(repository).buscarActivos("mat");
    }

    @Test
    void guardaLaEntidadRecibida() {
        CatalogoPrueba catalogo = new CatalogoPrueba(EstadoGeneral.ACTIVO);
        when(repository.save(catalogo)).thenReturn(catalogo);

        assertThat(service.guardar(catalogo)).isSameAs(catalogo);
        verify(repository).save(catalogo);
    }

    @Test
    void lanzaCuandoElIdentificadorNoExiste() {
        when(repository.findById((short) 10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId((short) 10))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void eliminaMedianteBajaLogica() {
        CatalogoPrueba catalogo = new CatalogoPrueba(EstadoGeneral.ACTIVO);
        when(repository.findById((short) 10)).thenReturn(Optional.of(catalogo));
        when(repository.save(catalogo)).thenReturn(catalogo);

        service.eliminarPorId((short) 10);

        assertThat(catalogo.getEstado()).isEqualTo(EstadoGeneral.INACTIVO);
        verify(repository).save(catalogo);
    }

    private static final class CatalogoPrueba implements CatalogoEntidad {
        private EstadoGeneral estado;

        private CatalogoPrueba(EstadoGeneral estado) {
            this.estado = estado;
        }

        @Override
        public EstadoGeneral getEstado() {
            return estado;
        }

        @Override
        public void setEstado(EstadoGeneral estado) {
            this.estado = estado;
        }
    }
}
