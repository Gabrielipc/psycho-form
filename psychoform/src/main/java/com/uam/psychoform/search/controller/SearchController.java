package com.uam.psychoform.search.controller;

import com.uam.psychoform.academic.repository.ParticipanteRepository;
import com.uam.psychoform.assessment.repository.SesionAplicacionRepository;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.security.SecurityPermissions;
import java.util.List;
import java.util.Locale;
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
        List<SearchResult> participantRows = participantes.findAll().stream()
                .filter(p -> contains(p.getCodigoParticipante(), query) || contains(p.getNombres(), query)
                        || contains(p.getApellidos(), query))
                .limit(8)
                .map(p -> new SearchResult("participante", String.valueOf(p.getId()),
                        p.getCodigoParticipante() + " - " + p.getNombres() + " " + p.getApellidos(),
                        "/app/participantes"))
                .toList();
        List<SearchResult> sessionRows = sesiones.findAll().stream()
                .filter(s -> contains(s.getCodigoSesion(), query) || contains(s.getNombreSesion(), query))
                .limit(8)
                .map(s -> new SearchResult("sesion", String.valueOf(s.getId()),
                        s.getCodigoSesion() + " - " + s.getNombreSesion(), "/app/sesiones/" + s.getId()))
                .toList();
        return ApiResponse.ok(List.of(participantRows, sessionRows).stream().flatMap(List::stream).limit(12).toList());
    }

    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    public record SearchResult(String type, String id, String label, String path) {
    }
}
