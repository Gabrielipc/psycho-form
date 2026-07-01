package com.uam.psychoform.search.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.uam.psychoform.academic.model.Participante;
import com.uam.psychoform.academic.repository.ParticipanteRepository;
import com.uam.psychoform.assessment.model.SesionAplicacion;
import com.uam.psychoform.assessment.repository.SesionAplicacionRepository;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.security.SecurityPermissions;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.prepost.PreAuthorize;

class SearchControllerTest {
    private final ParticipanteRepository participantes = Mockito.mock(ParticipanteRepository.class);
    private final SesionAplicacionRepository sesiones = Mockito.mock(SesionAplicacionRepository.class);
    private final SearchController controller = new SearchController(participantes, sesiones);

    @Test
    void searchRequiereUsuarioAutenticado() throws Exception {
        Method method = SearchController.class.getMethod("search", String.class);

        assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo(SecurityPermissions.AUTHENTICATED);
    }

    @Test
    void searchIgnoraQueriesMenoresADosCaracteresSinConsultarRepositorios() {
        ApiResponse<?> response = controller.search(" a ");

        assertThat(response.data()).isEqualTo(List.of());
        verifyNoInteractions(participantes, sesiones);
    }

    @Test
    void searchDevuelveParticipantesYSesionesQueCoinciden() {
        Participante participante = new Participante();
        participante.setId(UUID.randomUUID());
        participante.setCodigoParticipante("P-001");
        participante.setNombres("Ana");
        participante.setApellidos("Lopez");
        SesionAplicacion sesion = new SesionAplicacion();
        sesion.setId(7L);
        sesion.setCodigoSesion("SES-ANA");
        sesion.setNombreSesion("Aplicacion Ana");
        when(participantes.findAll()).thenReturn(List.of(participante));
        when(sesiones.findAll()).thenReturn(List.of(sesion));

        @SuppressWarnings("unchecked")
        List<SearchController.SearchResult> results = (List<SearchController.SearchResult>) controller.search("ana").data();

        assertThat(results).extracting(SearchController.SearchResult::type)
                .containsExactly("participante", "sesion");
        assertThat(results).extracting(SearchController.SearchResult::path)
                .containsExactly("/app/participantes", "/app/sesiones/7");
    }
}
