package com.uam.psychoform.instrument.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OptionRequest(@NotBlank String code, String text, @NotNull Integer order, BigDecimal ordinalValue) {
}