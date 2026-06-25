package com.uam.psychoform;

import static org.assertj.core.api.Assertions.assertThat;

import com.uam.psychoform.assessment.model.AsignacionTest;
import com.uam.psychoform.assessment.model.EstadoAsignacion;
import com.uam.psychoform.assessment.model.EstadoIntento;
import com.uam.psychoform.assessment.model.IntentoTest;
import com.uam.psychoform.scoring.model.Resultado;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class EntityAccessorCompileTest {

    @Test
    void operatedEntitiesExposeGetterSetterAccessors() {
        AsignacionTest asignacion = new AsignacionTest();
        asignacion.setEstado(EstadoAsignacion.ASIGNADO);

        IntentoTest intento = new IntentoTest();
        intento.setAsignacion(asignacion);
        intento.setEstado(EstadoIntento.NO_INICIADO);

        Resultado resultado = new Resultado();
        resultado.setIntento(intento);
        resultado.setPuntajeTotalDirecto(BigDecimal.ZERO);

        assertThat(resultado.getIntento().getAsignacion().getEstado()).isEqualTo(EstadoAsignacion.ASIGNADO);
    }
}
