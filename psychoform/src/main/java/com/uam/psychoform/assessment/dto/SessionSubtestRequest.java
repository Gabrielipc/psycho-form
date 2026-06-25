package com.uam.psychoform.assessment.dto;

import jakarta.validation.constraints.NotNull;

public record SessionSubtestRequest(@NotNull Long subtestId, @NotNull Integer order, Integer timeLimitSeconds,
        Boolean randomizeItems, Boolean randomizeOptions) {
}