package com.uam.psychoform.assessment.controller;

import com.uam.psychoform.assessment.dto.*;

import com.uam.psychoform.assessment.service.*;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.dto.EntityView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
public class ParticipantRuntimeController {
    private final ParticipantJwtService participantJwt;
    private final ParticipantAccessService access;
    private final ParticipantRuntimeService runtime;
    private final ParticipantAnswerService answers;
    private final ParticipantEvaluationView evaluation;

    public ParticipantRuntimeController(ParticipantJwtService participantJwt, ParticipantRuntimeService runtime,
            ParticipantAccessService access, ParticipantAnswerService answers, ParticipantEvaluationView evaluation) {
        this.participantJwt = participantJwt;
        this.access = access;
        this.runtime = runtime;
        this.answers = answers;
        this.evaluation = evaluation;
    }

    @GetMapping("/evaluacion-participante/yo")
    public ApiResponse<?> me(HttpServletRequest request) {
        var participantAccess = requireAccess(request);
        return ApiResponse.ok(evaluation.getEvaluationPayload(participantAccess.assignmentId()));
    }

    @PostMapping("/evaluacion-participante/iniciar")
    public ApiResponse<?> start(@Valid @RequestBody StartAttemptRequest body, HttpServletRequest request) {
        var participantAccess = requireAccess(request);
        if (!participantAccess.assignmentId().equals(body.assignmentId())) {
            throw new org.springframework.security.access.AccessDeniedException("Asignacion fuera de alcance");
        }
        return ApiResponse.ok(EntityView
                .of(runtime.startOrResumeAttempt(participantAccess, body.deviceInfo(), request.getRemoteAddr())));
    }

    @PostMapping("/intentos/{attemptId}/subtests/{subtestId}/iniciar")
    public ApiResponse<?> startSubtest(@PathVariable Long attemptId, @PathVariable Long subtestId,
            HttpServletRequest request) {
        var participantAccess = requireAccess(request);
        return ApiResponse.ok(EntityView.of(runtime.startSubtest(participantAccess, attemptId, subtestId)));
    }

    @GetMapping("/intentos/{attemptId}/subtests/{subtestId}/items")
    public ApiResponse<?> items(@PathVariable Long attemptId, @PathVariable Long subtestId,
            HttpServletRequest request) {
        var participantAccess = requireAccess(request);
        return ApiResponse.ok(evaluation.getScopedSubtestPayload(participantAccess, attemptId, subtestId));
    }

    @PutMapping("/intentos/{attemptId}/items/{itemId}/respuesta")
    public ApiResponse<?> answer(@PathVariable Long attemptId, @PathVariable Long itemId,
            @Valid @RequestBody SaveAnswerRequest body, HttpServletRequest request) {
        var participantAccess = requireAccess(request);
        return ApiResponse
                .ok(EntityView.of(answers.saveAnswer(participantAccess,
                        new ParticipantAnswerService.SaveAnswerCommand(attemptId, itemId,
                        body.selectedOptionIds(), body.textAnswer(), body.numericAnswer(), body.timeUsedSeconds()))));
    }

    @PostMapping("/intentos/{attemptId}/respuestas/bulk-sync")
    public ApiResponse<?> bulk(@PathVariable Long attemptId, @Valid @RequestBody List<SaveAnswerRequest> body,
            HttpServletRequest request) {
        var participantAccess = requireAccess(request);
        return ApiResponse
                .ok(EntityView.of(answers.bulkSyncAnswers(participantAccess,
                        new ParticipantAnswerService.BulkSyncAnswersCommand(attemptId,
                        body.stream().map(r -> new ParticipantAnswerService.SaveAnswerCommand(attemptId, r.itemId(),
                                r.selectedOptionIds(), r.textAnswer(), r.numericAnswer(), r.timeUsedSeconds()))
                                .toList()))));
    }

    @PostMapping("/intentos/{attemptId}/subtests/{subtestId}/finalizar")
    public ApiResponse<?> finishSubtest(@PathVariable Long attemptId, @PathVariable Long subtestId,
            @Valid @RequestBody FinishRequest body, HttpServletRequest request) {
        var participantAccess = requireAccess(request);
        return ApiResponse.ok(EntityView.of(runtime.finishSubtest(participantAccess, attemptId, subtestId,
                body.timeSeconds())));
    }

    @PostMapping("/intentos/{attemptId}/finalizar")
    public ApiResponse<?> finish(@PathVariable Long attemptId, @Valid @RequestBody FinishRequest body,
            HttpServletRequest request) {
        var participantAccess = requireAccess(request);
        return ApiResponse.ok(EntityView.of(runtime.finishAttempt(participantAccess, attemptId, body.timeSeconds())));
    }

    private ParticipantAccessService.ParticipantAccess requireAccess(HttpServletRequest request) {
        return access.validateRuntimeAccess(participantJwt.requireAccess(request.getHeader("Authorization")));
    }
}
