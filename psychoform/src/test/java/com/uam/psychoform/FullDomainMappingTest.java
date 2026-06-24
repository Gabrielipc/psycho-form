package com.uam.psychoform;

import static org.assertj.core.api.Assertions.assertThat;

import com.uam.psychoform.assessment.entity.OpcionSeleccionadaRespuesta;
import com.uam.psychoform.instrument.entity.ReglaCalificacion;
import com.uam.psychoform.instrument.entity.VersionTest;
import com.uam.psychoform.scoring.entity.ResultadoDimension;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Field;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.junit.jupiter.api.Test;

class FullDomainMappingTest {

    @Test
    void mapsBusinessColumnsRelationshipsAndDatabaseSpecificTypes() throws Exception {
        Field versionTest = VersionTest.class.getDeclaredField("test");
        Field ruleParameters = ReglaCalificacion.class.getDeclaredField("parametros");
        Field selectedOption = OpcionSeleccionadaRespuesta.class.getDeclaredField("opcion");
        Field normRange = ResultadoDimension.class.getDeclaredField("rangoBaremo");

        assertThat(versionTest.getAnnotation(ManyToOne.class)).isNotNull();
        assertThat(ruleParameters.getAnnotation(JdbcTypeCode.class).value()).isEqualTo(SqlTypes.JSON);
        assertThat(selectedOption.getAnnotation(ManyToOne.class)).isNotNull();
        assertThat(normRange.getAnnotation(ManyToOne.class)).isNotNull();
    }

    @Test
    void exposesBeanValidationForRequiredBusinessData() throws Exception {
        assertThat(VersionTest.class.getDeclaredField("numeroVersion").getAnnotation(NotBlank.class)).isNotNull();
        assertThat(ReglaCalificacion.class.getDeclaredField("prioridad").getAnnotation(NotNull.class)).isNotNull();
    }
}
