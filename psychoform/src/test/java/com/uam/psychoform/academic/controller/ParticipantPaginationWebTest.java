package com.uam.psychoform.academic.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.uam.psychoform.academic.model.Participante;
import com.uam.psychoform.academic.repository.*;
import com.uam.psychoform.academic.service.ParticipanteService;
import com.uam.psychoform.controller.GlobalExceptionHandler;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ParticipantPaginationWebTest {
    private ParticipanteRepository repository;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        repository = mock(ParticipanteRepository.class);
        var service = new ParticipanteService(repository, mock(CatalogoSexoRepository.class),
                mock(CarreraRepository.class), mock(CohorteRepository.class),
                mock(GrupoAcademicoRepository.class), Clock.systemUTC());
        mvc = MockMvcBuilders.standaloneSetup(new ParticipantController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        List<Participante> rows = List.of(participant(1), participant(2), participant(3));
        when(repository.findAll(any(Pageable.class))).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(0);
            int start = (int) Math.min(pageable.getOffset(), rows.size());
            int end = Math.min(start + pageable.getPageSize(), rows.size());
            return new PageImpl<>(rows.subList(start, end), pageable, rows.size());
        });
    }

    @Test
    void returnsDifferentPagesAndMetadata() throws Exception {
        mvc.perform(get("/participantes").param("page", "0").param("size", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].codigoParticipante").value("P-001"))
                .andExpect(jsonPath("$.data.content[1].codigoParticipante").value("P-002"))
                .andExpect(jsonPath("$.data.number").value(0))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(false));
        mvc.perform(get("/participantes").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].codigoParticipante").value("P-003"))
                .andExpect(jsonPath("$.data.number").value(1))
                .andExpect(jsonPath("$.data.numberOfElements").value(1))
                .andExpect(jsonPath("$.data.first").value(false))
                .andExpect(jsonPath("$.data.last").value(true));
        verify(repository).findAll(PageRequest.of(0, 2, Sort.by("codigoParticipante", "id")));
        verify(repository).findAll(PageRequest.of(1, 2, Sort.by("codigoParticipante", "id")));
        verify(repository, never()).findAll();
    }

    @Test
    void supportsDifferentPageSizes() throws Exception {
        mvc.perform(get("/participantes").param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].codigoParticipante").value("P-002"))
                .andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(3));
    }

    @Test
    void usesDefaultPageAndSize() throws Exception {
        mvc.perform(get("/participantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.number").value(0))
                .andExpect(jsonPath("$.data.size").value(20));
        verify(repository).findAll(PageRequest.of(0, 20, Sort.by("codigoParticipante", "id")));
    }

    @Test
    void returnsEmptyContentForPageBeyondLast() throws Exception {
        mvc.perform(get("/participantes").param("page", "5").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.number").value(5))
                .andExpect(jsonPath("$.data.totalElements").value(3));
    }

    @ParameterizedTest
    @CsvSource({"-1,2", "0,0", "0,-1", "0,101"})
    void rejectsInvalidPaginationWithoutQueryingRepository(int page, int size) throws Exception {
        mvc.perform(get("/participantes").param("page", String.valueOf(page))
                .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
        verifyNoInteractions(repository);
    }

    private static Participante participant(int number) {
        Participante participant = new Participante();
        participant.setId(new UUID(0, number));
        participant.setCodigoParticipante("P-00" + number);
        participant.setNombres("Nombre " + number);
        participant.setApellidos("Apellido " + number);
        return participant;
    }
}
