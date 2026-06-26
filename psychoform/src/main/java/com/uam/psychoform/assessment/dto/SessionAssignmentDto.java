package com.uam.psychoform.assessment.dto;

import java.util.UUID;

public record SessionAssignmentDto(
    Long assignmentId,
    UUID participantId,
    String participantName,
    String participantCode,
    String status,         // "GENERADO" | "ACTIVO" | "VENCIDO" | "REVOCADO"
    String state,          // "no-iniciado" | "en-progreso" | "completado" | "interrumpido" | "anulado"
    String currentSubtestId,
    Integer overallProgress,
    String lastActivity,
    String tokenExpiraEn
) {}
