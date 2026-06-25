package com.uam.psychoform.reporting.controller;

import com.uam.psychoform.reporting.service.ResultQueryService;
import com.uam.psychoform.scoring.service.ClaveSimpleScoringService;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.dto.EntityView;
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
    public ApiResponse<?> score(@PathVariable Long attemptId) {
        return ApiResponse.ok(EntityView.of(scoring.scoreAttempt(attemptId)));
    }

    @GetMapping("/attempts/{attemptId}/result")
    public ApiResponse<?> attemptResult(@PathVariable Long attemptId) {
        return ApiResponse.ok(queries.getAttemptResult(attemptId));
    }

    @GetMapping("/analytics/sessions/{id}/summary")
    public ApiResponse<?> sessionSummary(@PathVariable Long id) {
        return ApiResponse.ok(queries.getSessionSummary(id));
    }

    @GetMapping("/analytics/results")
    public ApiResponse<?> aggregate(@RequestParam Long sessionId) {
        return ApiResponse.ok(queries.getDimensionAverages(new ResultQueryService.ResultFilter(sessionId)));
    }

    @GetMapping("/dashboard/overview")
    public ApiResponse<?> overview(@RequestParam Long sessionId) {
        return ApiResponse.ok(queries.getSessionSummary(sessionId));
    }
}
