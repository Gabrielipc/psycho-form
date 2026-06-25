package com.uam.psychoform.assessment.dto;

import jakarta.validation.constraints.NotNull;

public record StartAttemptRequest(@NotNull Long assignmentId, String deviceInfo) {
}