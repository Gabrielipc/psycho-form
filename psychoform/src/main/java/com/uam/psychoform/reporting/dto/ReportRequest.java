package com.uam.psychoform.reporting.dto;

import com.uam.psychoform.reporting.model.FormatoReporte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportRequest(@NotBlank String type, @NotNull FormatoReporte format, @NotBlank String storagePath,
        String filtersJson, Long attemptId, Long resultId, Long sessionId) {
}