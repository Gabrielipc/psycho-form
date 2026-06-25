package com.uam.psychoform.academic.dto;

import jakarta.validation.constraints.NotBlank;

public record ParticipantRequest(@NotBlank String code, @NotBlank String firstNames, @NotBlank String lastNames) {
}