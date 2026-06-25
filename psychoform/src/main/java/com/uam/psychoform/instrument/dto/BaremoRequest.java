package com.uam.psychoform.instrument.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BaremoRequest(@NotNull Long versionId, Long dimensionId, @NotBlank String code,
        @NotBlank String name, String description, String normativeGroup) {
}