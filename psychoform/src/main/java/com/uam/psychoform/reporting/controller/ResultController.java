package com.uam.psychoform.reporting.controller;

import com.uam.psychoform.reporting.service.ResultQueryService;
import com.uam.psychoform.scoring.service.ClaveSimpleScoringService;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.dto.EntityView;
import com.uam.psychoform.security.SecurityPermissions;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class ResultController {
    private final ClaveSimpleScoringService scoring;
    private final ResultQueryService queries;

    public ResultController(ClaveSimpleScoringService scoring, ResultQueryService queries) {
        this.scoring = scoring;
        this.queries = queries;
    }

    @PostMapping("/attempts/{attemptId}/score")
    @PreAuthorize(SecurityPermissions.CALIFICACION_EJECUTAR)
    public ApiResponse<?> score(@PathVariable Long attemptId) {
        return ApiResponse.ok(EntityView.of(scoring.scoreAttempt(attemptId)));
    }

    @GetMapping("/attempts/{attemptId}/result")
    @PreAuthorize(SecurityPermissions.RESULTADO_VER)
    public ApiResponse<?> attemptResult(@PathVariable Long attemptId) {
        return ApiResponse.ok(queries.getAttemptResult(attemptId));
    }

    @GetMapping("/analytics/sessions/{id}/summary")
    @PreAuthorize(SecurityPermissions.RESULTADO_AGREGADO_VER)
    public ApiResponse<?> sessionSummary(@PathVariable Long id) {
        return ApiResponse.ok(queries.getSessionSummary(id));
    }

    @GetMapping("/analytics/results")
    @PreAuthorize(SecurityPermissions.RESULTADO_AGREGADO_VER)
    public ApiResponse<?> aggregate(@RequestParam Long sessionId) {
        return ApiResponse.ok(queries.getDimensionAverages(new ResultQueryService.ResultFilter(sessionId)));
    }

    @GetMapping("/dashboard/overview")
    @PreAuthorize(SecurityPermissions.RESULTADO_AGREGADO_VER)
    public ApiResponse<?> overview(@RequestParam Long sessionId) {
        return ApiResponse.ok(queries.getSessionSummary(sessionId));
    }
}
