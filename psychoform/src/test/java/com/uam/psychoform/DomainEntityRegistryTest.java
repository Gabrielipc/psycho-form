package com.uam.psychoform;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DomainEntityRegistryTest {

    @Test
    void mapsEveryRemainingBfaTableToAnEntity() throws Exception {
        Map<String, String> entities = Map.ofEntries(
                Map.entry("com.uam.psychoform.academic.entity.CatalogoSexo", "catalogo_sexo"),
                Map.entry("com.uam.psychoform.academic.entity.Carrera", "carrera"),
                Map.entry("com.uam.psychoform.academic.entity.Cohorte", "cohorte"),
                Map.entry("com.uam.psychoform.academic.entity.GrupoAcademico", "grupo_academico"),
                Map.entry("com.uam.psychoform.academic.entity.Participante", "participante"),
                Map.entry("com.uam.psychoform.security.entity.UsuarioRol", "usuario_rol"),
                Map.entry("com.uam.psychoform.security.entity.RolPermiso", "rol_permiso"),
                Map.entry("com.uam.psychoform.instrument.entity.EstrategiaCalificacion", "estrategia_calificacion"),
                Map.entry("com.uam.psychoform.instrument.entity.TestPsicologico", "test"),
                Map.entry("com.uam.psychoform.instrument.entity.VersionTest", "version_test"),
                Map.entry("com.uam.psychoform.instrument.entity.Subtest", "subtest"),
                Map.entry("com.uam.psychoform.instrument.entity.DimensionResultado", "dimension_resultado"),
                Map.entry("com.uam.psychoform.instrument.entity.Item", "item"),
                Map.entry("com.uam.psychoform.instrument.entity.RecursoMultimedia", "recurso_multimedia"),
                Map.entry("com.uam.psychoform.instrument.entity.ImagenItem", "imagen_item"),
                Map.entry("com.uam.psychoform.instrument.entity.OpcionItem", "opcion_item"),
                Map.entry("com.uam.psychoform.instrument.entity.ImagenOpcion", "imagen_opcion"),
                Map.entry("com.uam.psychoform.instrument.entity.ReglaCalificacion", "regla_calificacion"),
                Map.entry("com.uam.psychoform.instrument.entity.ClaveRespuesta", "clave_respuesta"),
                Map.entry("com.uam.psychoform.instrument.entity.OpcionPuntajeDimension", "opcion_puntaje_dimension"),
                Map.entry("com.uam.psychoform.instrument.entity.Baremo", "baremo"),
                Map.entry("com.uam.psychoform.instrument.entity.RangoBaremo", "rango_baremo"),
                Map.entry("com.uam.psychoform.instrument.entity.RubricaEvaluacion", "rubrica_evaluacion"),
                Map.entry("com.uam.psychoform.instrument.entity.CriterioRubrica", "criterio_rubrica"),
                Map.entry("com.uam.psychoform.instrument.entity.NivelCriterioRubrica", "nivel_criterio_rubrica"),
                Map.entry("com.uam.psychoform.assessment.entity.SesionAplicacion", "sesion_aplicacion"),
                Map.entry("com.uam.psychoform.assessment.entity.SesionSubtest", "sesion_subtest"),
                Map.entry("com.uam.psychoform.assessment.entity.AsignacionTest", "asignacion_test"),
                Map.entry("com.uam.psychoform.assessment.entity.IntentoTest", "intento_test"),
                Map.entry("com.uam.psychoform.assessment.entity.IntentoSubtest", "intento_subtest"),
                Map.entry("com.uam.psychoform.assessment.entity.RespuestaItem", "respuesta_item"),
                Map.entry("com.uam.psychoform.assessment.entity.OpcionSeleccionadaRespuesta", "opcion_seleccionada_respuesta"),
                Map.entry("com.uam.psychoform.scoring.entity.Resultado", "resultado"),
                Map.entry("com.uam.psychoform.scoring.entity.ResultadoDimension", "resultado_dimension"),
                Map.entry("com.uam.psychoform.scoring.entity.CalificacionRespuesta", "calificacion_respuesta"),
                Map.entry("com.uam.psychoform.scoring.entity.RevisionManualRespuesta", "revision_manual_respuesta"),
                Map.entry("com.uam.psychoform.scoring.entity.RevisionRubricaRespuesta", "revision_rubrica_respuesta"),
                Map.entry("com.uam.psychoform.reporting.entity.ReporteGenerado", "reporte_generado"),
                Map.entry("com.uam.psychoform.audit.entity.Auditoria", "auditoria"));

        for (Map.Entry<String, String> entry : entities.entrySet()) {
            Class<?> entity = Class.forName(entry.getKey());
            assertThat(entity.isAnnotationPresent(Entity.class)).isTrue();
            assertThat(entity.getAnnotation(Table.class).name()).isEqualTo(entry.getValue());
        }
    }
}
