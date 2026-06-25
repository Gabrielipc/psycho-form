package com.uam.psychoform.reporting.controller;

import com.uam.psychoform.reporting.dto.ReportRequest;

import com.uam.psychoform.reporting.model.FormatoReporte;
import com.uam.psychoform.reporting.service.ReportRegistryService;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.dto.EntityView;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportRegistryService reports;

    public ReportController(ReportRegistryService reports) {
        this.reports = reports;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(required = false) Long attemptId,
            @RequestParam(required = false) Long resultId, @RequestParam(required = false) Long sessionId) {
        return ApiResponse.ok(EntityView.of(reports.listReports(new ReportRegistryService.ReportFilter(attemptId, resultId, sessionId))));
    }

    @PostMapping("/individual")
    public ApiResponse<?> individual(@Valid @RequestBody ReportRequest request) {
        return ApiResponse.ok(EntityView.of(reports.registerIndividualReport(toCommand(request))));
    }

    @PostMapping("/aggregate")
    public ApiResponse<?> aggregate(@Valid @RequestBody ReportRequest request) {
        return ApiResponse.ok(EntityView.of(reports.registerAggregateReport(toCommand(request))));
    }

    private ReportRegistryService.RegisterReportCommand toCommand(ReportRequest request) {
        return new ReportRegistryService.RegisterReportCommand(request.type(), request.format(), request.storagePath(),
                request.filtersJson(), request.attemptId(), request.resultId(), request.sessionId());
    }
}

