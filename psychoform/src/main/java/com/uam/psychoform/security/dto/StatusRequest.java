package com.uam.psychoform.security.dto;

import com.uam.psychoform.security.model.EstadoGeneral;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull EstadoGeneral status) {
}