package com.uam.psychoform.academic.controller;

import com.uam.psychoform.academic.dto.ParticipantRequest;

import com.uam.psychoform.academic.model.Participante;
import com.uam.psychoform.academic.repository.ParticipanteRepository;
import com.uam.psychoform.academic.service.ParticipanteService;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.dto.EntityView;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/participantes")
public class ParticipantController {
    private final ParticipanteRepository repository;
    private final ParticipanteService service;

    public ParticipantController(ParticipanteRepository repository, ParticipanteService service) {
        this.repository = repository;
        this.service = service;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.ok(EntityView.of(repository.findAll()));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable UUID id) {
        return ApiResponse.ok(EntityView.of(repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Participante no encontrado: " + id))));
    }

    @PostMapping
    public ApiResponse<Participante> create(@Valid @RequestBody ParticipantRequest request) {
        return ApiResponse.ok(service.registrar(request.code(), request.firstNames(), request.lastNames()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deactivate(@PathVariable UUID id) {
        service.desactivar(id);
        return ApiResponse.ok(null);
    }
}

