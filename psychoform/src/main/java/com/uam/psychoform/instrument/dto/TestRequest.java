package com.uam.psychoform.instrument.dto;

import jakarta.validation.constraints.NotBlank;

public record TestRequest(@NotBlank String code, @NotBlank String name, String description) {
}