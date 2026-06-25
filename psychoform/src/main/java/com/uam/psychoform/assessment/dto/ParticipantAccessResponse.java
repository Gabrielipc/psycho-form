package com.uam.psychoform.assessment.dto;

import java.util.UUID;

public record ParticipantAccessResponse(Long assignmentId, UUID participantId, String accessToken, String tokenType) {
}