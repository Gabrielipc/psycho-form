package com.uam.psychoform.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateSessionRequest(@NotNull Long versionTestId, @NotBlank String code, @NotBlank String name,
        String description, @NotNull LocalDateTime scheduledStart, LocalDateTime scheduledEnd, String location) {
}