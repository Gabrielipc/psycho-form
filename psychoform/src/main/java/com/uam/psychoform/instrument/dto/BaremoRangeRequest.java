package com.uam.psychoform.instrument.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record BaremoRangeRequest(@NotNull BigDecimal minScore, @NotNull BigDecimal maxScore,
        BigDecimal percentile, @NotBlank String category, String interpretation, String recommendation,
        @NotNull Integer order) {
}