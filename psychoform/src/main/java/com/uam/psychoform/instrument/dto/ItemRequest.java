package com.uam.psychoform.instrument.dto;

import com.uam.psychoform.instrument.model.TipoItem;
import com.uam.psychoform.instrument.model.TipoRespuesta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ItemRequest(@NotBlank String code, @NotNull TipoItem itemType, @NotNull TipoRespuesta responseType,
        String prompt, String instruction, @NotNull Integer order, BigDecimal baseScore, Integer timeLimitSeconds,
        Boolean required, Boolean confidential) {
}