package com.uam.psychoform.assessment.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.uam.psychoform.assessment.entity.EstadoSesionAplicacion;
import com.uam.psychoform.assessment.entity.SesionAplicacion;
import com.uam.psychoform.assessment.repository.SesionAplicacionRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SesionAplicacionServiceTest {
    @Test
    void rechazaCerrarUnaSesionProgramada() {
        SesionAplicacionRepository repository = Mockito.mock(SesionAplicacionRepository.class);
        SesionAplicacion sesion = new SesionAplicacion();
        sesion.setEstado(EstadoSesionAplicacion.PROGRAMADA);
        when(repository.findByIdForUpdate(10L)).thenReturn(Optional.of(sesion));
        SesionAplicacionService service = new SesionAplicacionService(repository);

        assertThatThrownBy(() -> service.cerrar(10L)).isInstanceOf(IllegalStateException.class);
    }
}
