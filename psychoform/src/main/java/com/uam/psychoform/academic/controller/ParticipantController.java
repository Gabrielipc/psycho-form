package com.uam.psychoform.academic.controller;

import com.uam.psychoform.academic.dto.ParticipantRequest;

import com.uam.psychoform.academic.model.Participante;
import com.uam.psychoform.academic.service.ParticipanteService;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.dto.EntityView;
import com.uam.psychoform.security.SecurityPermissions;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/participantes")
public class ParticipantController {
    private final ParticipanteService service;

    public ParticipantController(ParticipanteService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize(SecurityPermissions.PARTICIPANTE_LEER)
    public ApiResponse<?> list() {
        return ApiResponse.ok(EntityView.of(service.listar()));
    }

    @GetMapping("/{id}")
    @PreAuthorize(SecurityPermissions.PARTICIPANTE_LEER)
    public ApiResponse<?> get(@PathVariable UUID id) {
        return ApiResponse.ok(EntityView.of(service.obtener(id)));
    }

    @PostMapping
    @PreAuthorize(SecurityPermissions.PARTICIPANTE_CREAR)
    public ApiResponse<Participante> create(@Valid @RequestBody ParticipantRequest request) {
        return ApiResponse.ok(service.registrar(request.code(), request.firstNames(), request.lastNames()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityPermissions.PARTICIPANTE_ELIMINAR)
    public ApiResponse<Void> deactivate(@PathVariable UUID id) {
        service.desactivar(id);
        return ApiResponse.ok(null);
    }
}
