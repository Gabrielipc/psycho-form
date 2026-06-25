package com.uam.psychoform.assessment.dto;

import jakarta.validation.constraints.NotNull;

public record FinishRequest(@NotNull Integer timeSeconds) {
}