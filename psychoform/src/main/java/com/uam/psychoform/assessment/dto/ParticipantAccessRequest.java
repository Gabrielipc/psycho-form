package com.uam.psychoform.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ParticipantAccessRequest(@NotNull Long assignmentId, @NotBlank String token) {
}