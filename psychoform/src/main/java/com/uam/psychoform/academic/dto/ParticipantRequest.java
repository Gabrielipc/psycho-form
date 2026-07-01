package com.uam.psychoform.academic.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record ParticipantRequest(
        @NotBlank String code,
        @NotBlank String firstNames,
        @NotBlank String lastNames,
        LocalDate fechaNacimiento,
        Short sexoId,
        Short carreraId,
        Short cohorteId,
        Short grupoAcademicoId) {
}
