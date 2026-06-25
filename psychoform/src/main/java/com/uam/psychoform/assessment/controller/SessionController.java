package com.uam.psychoform.assessment.controller;

import com.uam.psychoform.assessment.dto.*;

import com.uam.psychoform.assessment.service.*;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.dto.EntityView;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sesiones")
public class SessionController {
    private final SessionManagementService management;
    private final SesionAplicacionService lifecycle;
    private final ParticipantRuntimeService runtime;

    public SessionController(SessionManagementService management, SesionAplicacionService lifecycle,
            ParticipantRuntimeService runtime) {
        this.management = management;
        this.lifecycle = lifecycle;
        this.runtime = runtime;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.ok(EntityView.of(management.listSessions()));
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody CreateSessionRequest request) {
        return ApiResponse.ok(EntityView.of(management.create(new SessionManagementService.CreateSessionCommand(request.versionTestId(),
                request.code(), request.name(), request.description(), request.scheduledStart(), request.scheduledEnd(),
                request.location()))));
    }

    @PostMapping("/{id}/activar")
    public ApiResponse<?> activate(@PathVariable Long id) {
        return ApiResponse.ok(EntityView.of(lifecycle.abrir(id)));
    }

    @PostMapping("/{id}/cerrar")
    public ApiResponse<?> close(@PathVariable Long id) {
        return ApiResponse.ok(EntityView.of(lifecycle.cerrar(id)));
    }

    @PostMapping("/{id}/cancelar")
    public ApiResponse<?> cancel(@PathVariable Long id) {
        return ApiResponse.ok(EntityView.of(lifecycle.cancelar(id)));
    }

    @PutMapping("/{id}/subtests")
    public ApiResponse<?> subtests(@PathVariable Long id, @Valid @RequestBody List<SessionSubtestRequest> request) {
        return ApiResponse.ok(EntityView.of(management.replaceSubtests(id, request.stream()
                .map(r -> new SessionManagementService.SessionSubtestCommand(r.subtestId(), r.order(),
                        r.timeLimitSeconds(), r.randomizeItems(), r.randomizeOptions()))
                .toList())));
    }

    @PostMapping("/{id}/asignaciones")
    public ApiResponse<?> assign(@PathVariable Long id, @Valid @RequestBody AssignParticipantRequest request) {
        return ApiResponse.ok(runtime.assignParticipant(new ParticipantRuntimeService.AssignParticipantCommand(id,
                request.participantId(), Duration.ofHours(request.ttlHours() == null ? 8 : request.ttlHours()))));
    }
}

