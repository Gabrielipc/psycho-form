package com.uam.psychoform.reporting.controller;

import com.uam.psychoform.reporting.dto.ReportRequest;

import com.uam.psychoform.reporting.model.FormatoReporte;
import com.uam.psychoform.reporting.service.ReportDocumentService;
import com.uam.psychoform.reporting.service.ReportRegistryService;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.dto.EntityView;
import com.uam.psychoform.security.SecurityPermissions;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportRegistryService reports;
    private final ReportDocumentService documents;

    public ReportController(ReportRegistryService reports, ReportDocumentService documents) {
        this.reports = reports;
        this.documents = documents;
    }

    @GetMapping
    @PreAuthorize(SecurityPermissions.REPORTE_LEER)
    public ApiResponse<?> list(@RequestParam(required = false) Long attemptId,
            @RequestParam(required = false) Long resultId, @RequestParam(required = false) Long sessionId) {
        return ApiResponse.ok(EntityView.of(reports.listReports(new ReportRegistryService.ReportFilter(attemptId, resultId, sessionId))));
    }

    @PostMapping("/individual")
    @PreAuthorize(SecurityPermissions.REPORTE_EXPORTAR)
    public ApiResponse<?> individual(@Valid @RequestBody ReportRequest request) {
        return ApiResponse.ok(EntityView.of(reports.registerIndividualReport(toCommand(request))));
    }

    @PostMapping("/aggregate")
    @PreAuthorize(SecurityPermissions.REPORTE_EXPORTAR)
    public ApiResponse<?> aggregate(@Valid @RequestBody ReportRequest request) {
        return ApiResponse.ok(EntityView.of(reports.registerAggregateReport(toCommand(request))));
    }

    @GetMapping("/export")
    @PreAuthorize(SecurityPermissions.REPORTE_EXPORTAR)
    public ResponseEntity<byte[]> export(@RequestParam String type, @RequestParam FormatoReporte format,
            @RequestParam(required = false) Long attemptId, @RequestParam(required = false) Long sessionId) {
        ReportDocumentService.Document document = documents.export(type, format, attemptId, sessionId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(document.fileName()).build().toString())
                .body(document.bytes());
    }

    private ReportRegistryService.RegisterReportCommand toCommand(ReportRequest request) {
        return new ReportRegistryService.RegisterReportCommand(request.type(), request.format(), request.storagePath(),
                request.filtersJson(), request.attemptId(), request.resultId(), request.sessionId());
    }
}
