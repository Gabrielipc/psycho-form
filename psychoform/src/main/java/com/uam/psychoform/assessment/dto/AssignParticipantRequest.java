package com.uam.psychoform.assessment.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignParticipantRequest(@NotNull UUID participantId, Long ttlHours) {
}