package com.uam.psychoform.scoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.uam.psychoform.assessment.model.AsignacionTest;
import com.uam.psychoform.assessment.model.EstadoIntento;
import com.uam.psychoform.assessment.model.IntentoTest;
import com.uam.psychoform.assessment.model.OpcionSeleccionadaRespuesta;
import com.uam.psychoform.assessment.model.RespuestaItem;
import com.uam.psychoform.assessment.model.SesionAplicacion;
import com.uam.psychoform.assessment.repository.IntentoTestRepository;
import com.uam.psychoform.assessment.repository.OpcionSeleccionadaRespuestaRepository;
import com.uam.psychoform.assessment.repository.RespuestaItemRepository;
import com.uam.psychoform.instrument.model.ClaveRespuesta;
import com.uam.psychoform.instrument.model.EstadoConfiguracion;
import com.uam.psychoform.instrument.model.EstadoVersionTest;
import com.uam.psychoform.instrument.model.EstrategiaCalificacion;
import com.uam.psychoform.instrument.model.Item;
import com.uam.psychoform.instrument.model.OpcionItem;
import com.uam.psychoform.instrument.model.ReglaCalificacion;
import com.uam.psychoform.instrument.model.TipoEstrategiaCalificacion;
import com.uam.psychoform.instrument.model.VersionTest;
import com.uam.psychoform.instrument.repository.ClaveRespuestaLookupRepository;
import com.uam.psychoform.instrument.repository.BaremoRepository;
import com.uam.psychoform.instrument.repository.OpcionPuntajeDimensionRepository;
import com.uam.psychoform.instrument.repository.RangoBaremoRepository;
import com.uam.psychoform.scoring.model.Resultado;
import com.uam.psychoform.scoring.repository.CalificacionRespuestaRepository;
import com.uam.psychoform.scoring.repository.ResultadoDimensionRepository;
import com.uam.psychoform.scoring.repository.ResultadoRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.prepost.PreAuthorize;

class ClaveSimpleScoringServiceTest {
    private final IntentoTestRepository intentos = Mockito.mock(IntentoTestRepository.class);
    private final ResultadoRepository resultados = Mockito.mock(ResultadoRepository.class);
    private final RespuestaItemRepository respuestas = Mockito.mock(RespuestaItemRepository.class);
    private final OpcionSeleccionadaRespuestaRepository opcionesSeleccionadas = Mockito.mock(OpcionSeleccionadaRespuestaRepository.class);
    private final ClaveRespuestaLookupRepository claves = Mockito.mock(ClaveRespuestaLookupRepository.class);
    private final OpcionPuntajeDimensionRepository puntajesDimension = Mockito.mock(OpcionPuntajeDimensionRepository.class);
    private final BaremoRepository baremos = Mockito.mock(BaremoRepository.class);
    private final RangoBaremoRepository rangosBaremo = Mockito.mock(RangoBaremoRepository.class);
    private final CalificacionRespuestaRepository calificaciones = Mockito.mock(CalificacionRespuestaRepository.class);
    private final ResultadoDimensionRepository resultadosDimension = Mockito.mock(ResultadoDimensionRepository.class);
    private final UsuarioRepository usuarios = Mockito.mock(UsuarioRepository.class);
    private final CurrentActor currentActor = Mockito.mock(CurrentActor.class);
    private final ClaveSimpleScoringService service = new ClaveSimpleScoringService(intentos, resultados, respuestas,
            opcionesSeleccionadas, claves, puntajesDimension, baremos, rangosBaremo, calificaciones,
            resultadosDimension, usuarios, currentActor,
            Clock.fixed(Instant.parse("2026-06-24T12:00:00Z"), ZoneOffset.UTC));

    @Test
    void scoreAttemptRequierePermisoCalificacion() throws Exception {
        Method method = ClaveSimpleScoringService.class.getMethod("scoreAttempt", long.class);

        assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAuthority('PERM_CALIFICACION_EJECUTAR')");
    }

    @Test
    void scoreAttemptRetornaResultadoExistenteSinDuplicar() {
        Resultado existente = new Resultado();
        when(resultados.findByIntentoId(7L)).thenReturn(Optional.of(existente));

        assertThat(service.scoreAttempt(7L)).isSameAs(existente);
    }

    @Test
    void scoreAttemptCalculaClaveSimpleConTrazabilidadYActorActual() {
        UUID actorId = UUID.randomUUID();
        Usuario actor = new Usuario();
        actor.setId(actorId);
        IntentoTest intento = intentoPublicado();
        Item item = item(10L);
        OpcionItem correcta = opcion(20L, item);
        RespuestaItem respuesta = new RespuestaItem();
        respuesta.setId(30L);
        respuesta.setItem(item);
        OpcionSeleccionadaRespuesta seleccion = new OpcionSeleccionadaRespuesta();
        seleccion.setRespuesta(respuesta);
        seleccion.setOpcion(correcta);
        ClaveRespuesta clave = clave(item, correcta, new BigDecimal("2.00"));
        when(resultados.findByIntentoId(7L)).thenReturn(Optional.empty());
        when(intentos.findByIdForUpdate(7L)).thenReturn(Optional.of(intento));
        when(currentActor.usuarioId()).thenReturn(actorId);
        when(usuarios.findById(actorId)).thenReturn(Optional.of(actor));
        when(respuestas.findByIntentoIdWithItem(7L)).thenReturn(List.of(respuesta));
        when(opcionesSeleccionadas.findByRespuestaIdIn(List.of(30L))).thenReturn(List.of(seleccion));
        when(claves.findOfficialKeysByVersionId(100L)).thenReturn(List.of(clave));
        when(puntajesDimension.findActiveOfficialByVersionAndOptionIds(100L, List.of(20L))).thenReturn(List.of());

        Resultado resultado = service.scoreAttempt(7L);

        assertThat(resultado.getCalculadoPor()).isSameAs(actor);
        assertThat(resultado.getPuntajeTotalDirecto()).isEqualByComparingTo("2.00");
        assertThat(resultado.getCantidadItems()).isEqualTo(1);
        assertThat(resultado.getCantidadCorrectas()).isEqualTo(1);
        verify(calificaciones).save(Mockito.argThat(c -> c.getRespuesta() == respuesta
                && c.getReglaCalificacion() == clave.getReglaCalificacion()
                && c.getOpcion() == correcta
                && c.getEsCorrecta()
                && c.getPuntajeObtenido().compareTo(new BigDecimal("2.00")) == 0));
        verify(opcionesSeleccionadas, never()).findByRespuestaId(30L);
    }

    @Test
    void scoreAttemptRechazaIntentoNoCompletado() {
        IntentoTest intento = intentoPublicado();
        intento.setEstado(EstadoIntento.EN_PROGRESO);
        when(resultados.findByIntentoId(7L)).thenReturn(Optional.empty());
        when(intentos.findByIdForUpdate(7L)).thenReturn(Optional.of(intento));

        assertThatThrownBy(() -> service.scoreAttempt(7L)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("finalizado");
    }

    private static IntentoTest intentoPublicado() {
        EstrategiaCalificacion estrategia = new EstrategiaCalificacion();
        estrategia.setTipoEstrategia(TipoEstrategiaCalificacion.CLAVE_SIMPLE);
        VersionTest version = new VersionTest();
        version.setId(100L);
        version.setEstado(EstadoVersionTest.PUBLICADO);
        version.setEstrategiaCalificacion(estrategia);
        SesionAplicacion sesion = new SesionAplicacion();
        sesion.setVersionTest(version);
        AsignacionTest asignacion = new AsignacionTest();
        asignacion.setSesionAplicacion(sesion);
        IntentoTest intento = new IntentoTest();
        intento.setEstado(EstadoIntento.COMPLETADO);
        intento.setAsignacion(asignacion);
        return intento;
    }

    private static Item item(Long id) {
        Item item = new Item();
        item.setId(id);
        return item;
    }

    private static OpcionItem opcion(Long id, Item item) {
        OpcionItem opcion = new OpcionItem();
        opcion.setId(id);
        opcion.setItem(item);
        return opcion;
    }

    private static ClaveRespuesta clave(Item item, OpcionItem correcta, BigDecimal puntaje) {
        ReglaCalificacion regla = new ReglaCalificacion();
        regla.setEstado(EstadoConfiguracion.APROBADO);
        regla.setActiva(true);
        ClaveRespuesta clave = new ClaveRespuesta();
        clave.setItem(item);
        clave.setOpcionCorrecta(correcta);
        clave.setPuntaje(puntaje);
        clave.setRequiereRevisionManual(false);
        clave.setReglaCalificacion(regla);
        return clave;
    }
}
