package com.uam.psychoform;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DomainEntityRegistryTest {

    @Test
    void mapsEveryRemainingBfaTableToAnEntity() throws Exception {
        Map<String, String> entities = Map.ofEntries(
                Map.entry("com.uam.psychoform.academic.model.CatalogoSexo", "catalogo_sexo"),
                Map.entry("com.uam.psychoform.academic.model.Carrera", "carrera"),
                Map.entry("com.uam.psychoform.academic.model.Cohorte", "cohorte"),
                Map.entry("com.uam.psychoform.academic.model.GrupoAcademico", "grupo_academico"),
                Map.entry("com.uam.psychoform.academic.model.Participante", "participante"),
                Map.entry("com.uam.psychoform.security.model.UsuarioRol", "usuario_rol"),
                Map.entry("com.uam.psychoform.security.model.RolPermiso", "rol_permiso"),
                Map.entry("com.uam.psychoform.instrument.model.EstrategiaCalificacion", "estrategia_calificacion"),
                Map.entry("com.uam.psychoform.instrument.model.TestPsicologico", "test"),
                Map.entry("com.uam.psychoform.instrument.model.VersionTest", "version_test"),
                Map.entry("com.uam.psychoform.instrument.model.Subtest", "subtest"),
                Map.entry("com.uam.psychoform.instrument.model.DimensionResultado", "dimension_resultado"),
                Map.entry("com.uam.psychoform.instrument.model.Item", "item"),
                Map.entry("com.uam.psychoform.instrument.model.RecursoMultimedia", "recurso_multimedia"),
                Map.entry("com.uam.psychoform.instrument.model.ImagenItem", "imagen_item"),
                Map.entry("com.uam.psychoform.instrument.model.OpcionItem", "opcion_item"),
                Map.entry("com.uam.psychoform.instrument.model.ImagenOpcion", "imagen_opcion"),
                Map.entry("com.uam.psychoform.instrument.model.ReglaCalificacion", "regla_calificacion"),
                Map.entry("com.uam.psychoform.instrument.model.ClaveRespuesta", "clave_respuesta"),
                Map.entry("com.uam.psychoform.instrument.model.OpcionPuntajeDimension", "opcion_puntaje_dimension"),
                Map.entry("com.uam.psychoform.instrument.model.Baremo", "baremo"),
                Map.entry("com.uam.psychoform.instrument.model.RangoBaremo", "rango_baremo"),
                Map.entry("com.uam.psychoform.instrument.model.RubricaEvaluacion", "rubrica_evaluacion"),
                Map.entry("com.uam.psychoform.instrument.model.CriterioRubrica", "criterio_rubrica"),
                Map.entry("com.uam.psychoform.instrument.model.NivelCriterioRubrica", "nivel_criterio_rubrica"),
                Map.entry("com.uam.psychoform.assessment.model.SesionAplicacion", "sesion_aplicacion"),
                Map.entry("com.uam.psychoform.assessment.model.SesionSubtest", "sesion_subtest"),
                Map.entry("com.uam.psychoform.assessment.model.AsignacionTest", "asignacion_test"),
                Map.entry("com.uam.psychoform.assessment.model.IntentoTest", "intento_test"),
                Map.entry("com.uam.psychoform.assessment.model.IntentoSubtest", "intento_subtest"),
                Map.entry("com.uam.psychoform.assessment.model.RespuestaItem", "respuesta_item"),
                Map.entry("com.uam.psychoform.assessment.model.OpcionSeleccionadaRespuesta", "opcion_seleccionada_respuesta"),
                Map.entry("com.uam.psychoform.scoring.model.Resultado", "resultado"),
                Map.entry("com.uam.psychoform.scoring.model.ResultadoDimension", "resultado_dimension"),
                Map.entry("com.uam.psychoform.scoring.model.CalificacionRespuesta", "calificacion_respuesta"),
                Map.entry("com.uam.psychoform.scoring.model.RevisionManualRespuesta", "revision_manual_respuesta"),
                Map.entry("com.uam.psychoform.scoring.model.RevisionRubricaRespuesta", "revision_rubrica_respuesta"),
                Map.entry("com.uam.psychoform.reporting.model.ReporteGenerado", "reporte_generado"),
                Map.entry("com.uam.psychoform.audit.model.Auditoria", "auditoria"));

        for (Map.Entry<String, String> entry : entities.entrySet()) {
            Class<?> entity = Class.forName(entry.getKey());
            assertThat(entity.isAnnotationPresent(Entity.class)).isTrue();
            assertThat(entity.getAnnotation(Table.class).name()).isEqualTo(entry.getValue());
        }
    }

    @Test
    void noEntityRemainsAnIdentifierOnlyScaffold() throws Exception {
        String[] classNames = {
                "academic.model.CatalogoSexo", "academic.model.Carrera", "academic.model.Cohorte", "academic.model.GrupoAcademico", "academic.model.Participante",
                "assessment.model.SesionAplicacion", "assessment.model.SesionSubtest", "assessment.model.AsignacionTest", "assessment.model.IntentoTest", "assessment.model.IntentoSubtest", "assessment.model.RespuestaItem", "assessment.model.OpcionSeleccionadaRespuesta",
                "audit.model.Auditoria", "instrument.model.Baremo", "instrument.model.ClaveRespuesta", "instrument.model.CriterioRubrica", "instrument.model.DimensionResultado", "instrument.model.EstrategiaCalificacion", "instrument.model.ImagenItem", "instrument.model.ImagenOpcion", "instrument.model.Item", "instrument.model.NivelCriterioRubrica", "instrument.model.OpcionItem", "instrument.model.OpcionPuntajeDimension", "instrument.model.RangoBaremo", "instrument.model.RecursoMultimedia", "instrument.model.ReglaCalificacion", "instrument.model.RubricaEvaluacion", "instrument.model.Subtest", "instrument.model.TestPsicologico", "instrument.model.VersionTest",
                "reporting.model.ReporteGenerado", "scoring.model.CalificacionRespuesta", "scoring.model.Resultado", "scoring.model.ResultadoDimension", "scoring.model.RevisionManualRespuesta", "scoring.model.RevisionRubricaRespuesta"};
        for (String className : classNames) {
            Class<?> entity = Class.forName("com.uam.psychoform." + className);
            long persistentFields = Arrays.stream(entity.getDeclaredFields()).filter(field -> !java.lang.reflect.Modifier.isStatic(field.getModifiers())).count();
            assertThat(persistentFields).as(className).isGreaterThan(1);
        }
    }
}
