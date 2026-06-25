package com.uam.psychoform.instrument.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubtestRequest(@NotBlank String code, @NotBlank String name, String description, String instructions,
        @NotNull Integer order, Integer timeLimitSeconds, Boolean randomizeItems, Boolean randomizeOptions,
        Boolean required, Short strategyId) {
}