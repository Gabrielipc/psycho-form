package com.uam.psychoform.instrument.dto;

import com.uam.psychoform.instrument.model.TipoReglaCalificacion;
import jakarta.validation.constraints.NotNull;

public record ScoringRuleRequest(@NotNull Short strategyId, @NotNull TipoReglaCalificacion ruleType, Long itemId,
        Integer priority, String parametersJson, String observation) {
}