package com.uam.psychoform.assessment.controller;

import com.uam.psychoform.assessment.dto.*;

import com.uam.psychoform.assessment.repository.IntentoTestRepository;
import com.uam.psychoform.assessment.service.*;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.dto.EntityView;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
public class ParticipantRuntimeController {
    private final ParticipantJwtService participantJwt;
    private final ParticipantRuntimeService runtime;
    private final ParticipantAnswerService answers;
    private final ParticipantEvaluationView evaluation;
    private final IntentoTestRepository intentos;

    public ParticipantRuntimeController(ParticipantJwtService participantJwt, ParticipantRuntimeService runtime,
            ParticipantAnswerService answers, ParticipantEvaluationView evaluation, IntentoTestRepository intentos) {
        this.participantJwt = participantJwt;
        this.runtime = runtime;
        this.answers = answers;
        this.evaluation = evaluation;
        this.intentos = intentos;
    }

    @GetMapping("/evaluacion-participante/yo")
    public ApiResponse<?> me(HttpServletRequest request) {
        long assignmentId = participantJwt.requireAssignment(request.getHeader("Authorization"));
        return ApiResponse.ok(evaluation.getEvaluationPayload(assignmentId));
    }

    @PostMapping("/evaluacion-participante/iniciar")
    public ApiResponse<?> start(@Valid @RequestBody StartAttemptRequest body, HttpServletRequest request) {
        long assignmentId = participantJwt.requireAssignment(request.getHeader("Authorization"));
        if (assignmentId != body.assignmentId()) {
            throw new org.springframework.security.access.AccessDeniedException("Asignacion fuera de alcance");
        }
        return ApiResponse.ok(EntityView
                .of(runtime.startOrResumeAttempt(body.assignmentId(), body.deviceInfo(), request.getRemoteAddr())));
    }

    @PostMapping("/intentos/{attemptId}/subtests/{subtestId}/iniciar")
    public ApiResponse<?> startSubtest(@PathVariable Long attemptId, @PathVariable Long subtestId,
            HttpServletRequest request) {
        requireAttemptScope(request, attemptId);
        return ApiResponse.ok(EntityView.of(runtime.startSubtest(attemptId, subtestId)));
    }

    @GetMapping("/intentos/{attemptId}/subtests/{subtestId}/items")
    public ApiResponse<?> items(@PathVariable Long attemptId, @PathVariable Long subtestId,
            HttpServletRequest request) {
        requireAttemptScope(request, attemptId);
        return ApiResponse
                .ok(evaluation.getEvaluationPayload(requireAttempt(attemptId).getAsignacion().getId()).subtests()
                        .stream().filter(s -> s.subtestId() == subtestId).findFirst()
                        .orElseThrow(() -> new EntityNotFoundException("Subtest no encontrado en intento")));
    }

    @PutMapping("/intentos/{attemptId}/items/{itemId}/respuesta")
    public ApiResponse<?> answer(@PathVariable Long attemptId, @PathVariable Long itemId,
            @Valid @RequestBody SaveAnswerRequest body, HttpServletRequest request) {
        requireAttemptScope(request, attemptId);
        return ApiResponse
                .ok(EntityView.of(answers.saveAnswer(new ParticipantAnswerService.SaveAnswerCommand(attemptId, itemId,
                        body.selectedOptionIds(), body.textAnswer(), body.numericAnswer(), body.timeUsedSeconds()))));
    }

    @PostMapping("/intentos/{attemptId}/respuestas/bulk-sync")
    public ApiResponse<?> bulk(@PathVariable Long attemptId, @Valid @RequestBody List<SaveAnswerRequest> body,
            HttpServletRequest request) {
        requireAttemptScope(request, attemptId);
        return ApiResponse
                .ok(EntityView.of(answers.bulkSyncAnswers(new ParticipantAnswerService.BulkSyncAnswersCommand(attemptId,
                        body.stream().map(r -> new ParticipantAnswerService.SaveAnswerCommand(attemptId, r.itemId(),
                                r.selectedOptionIds(), r.textAnswer(), r.numericAnswer(), r.timeUsedSeconds()))
                                .toList()))));
    }

    @PostMapping("/intentos/{attemptId}/subtests/{subtestId}/finalizar")
    public ApiResponse<?> finishSubtest(@PathVariable Long attemptId, @PathVariable Long subtestId,
            @Valid @RequestBody FinishRequest body, HttpServletRequest request) {
        requireAttemptScope(request, attemptId);
        return ApiResponse.ok(EntityView.of(runtime.finishSubtest(attemptId, subtestId, body.timeSeconds())));
    }

    @PostMapping("/intentos/{attemptId}/finalizar")
    public ApiResponse<?> finish(@PathVariable Long attemptId, @Valid @RequestBody FinishRequest body,
            HttpServletRequest request) {
        requireAttemptScope(request, attemptId);
        return ApiResponse.ok(EntityView.of(runtime.finishAttempt(attemptId, body.timeSeconds())));
    }

    private void requireAttemptScope(HttpServletRequest request, long attemptId) {
        long assignmentId = participantJwt.requireAssignment(request.getHeader("Authorization"));
        if (!requireAttempt(attemptId).getAsignacion().getId().equals(assignmentId)) {
            throw new org.springframework.security.access.AccessDeniedException("Intento fuera de alcance");
        }
    }

    private com.uam.psychoform.assessment.model.IntentoTest requireAttempt(long attemptId) {
        return intentos.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Intento no encontrado: " + attemptId));
    }
}

