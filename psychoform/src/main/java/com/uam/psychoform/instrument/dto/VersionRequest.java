package com.uam.psychoform.instrument.dto;

import jakarta.validation.constraints.NotBlank;

public record VersionRequest(@NotBlank String number, Short strategyId, String instructions,
        Integer timeLimitSeconds, Boolean randomizeSubtests, Boolean randomizeItems) {
}