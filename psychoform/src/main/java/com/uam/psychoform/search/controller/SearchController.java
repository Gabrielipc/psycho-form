package com.uam.psychoform.search.controller;

import com.uam.psychoform.academic.repository.ParticipanteRepository;
import com.uam.psychoform.assessment.repository.SesionAplicacionRepository;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.search.dto.SearchResult;
import com.uam.psychoform.security.SecurityPermissions;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
public class SearchController {
    private final ParticipanteRepository participantes;
    private final SesionAplicacionRepository sesiones;

    public SearchController(ParticipanteRepository participantes, SesionAplicacionRepository sesiones) {
        this.participantes = participantes;
        this.sesiones = sesiones;
    }

    @GetMapping
    @PreAuthorize(SecurityPermissions.AUTHENTICATED)
    public ApiResponse<?> search(@RequestParam String q) {
        String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        if (query.length() < 2) {
            return ApiResponse.ok(List.of());
        }
        List<SearchResult> participantRows = participantes.searchByTerm(query, PageRequest.of(0, 8)).stream()
                .map(p -> new SearchResult("participante", String.valueOf(p.getId()),
                        p.getCodigoParticipante() + " - " + p.getNombres() + " " + p.getApellidos(),
                        "/app/participantes"))
                .toList();
        List<SearchResult> sessionRows = sesiones.searchByTerm(query, PageRequest.of(0, 8)).stream()
                .map(s -> new SearchResult("sesion", String.valueOf(s.getId()),
                        s.getCodigoSesion() + " - " + s.getNombreSesion(), "/app/sesiones/" + s.getId()))
                .toList();
        return ApiResponse.ok(List.of(participantRows, sessionRows).stream().flatMap(List::stream).limit(12).toList());
    }

}
