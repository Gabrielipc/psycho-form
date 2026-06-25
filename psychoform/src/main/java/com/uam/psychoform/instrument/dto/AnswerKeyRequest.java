package com.uam.psychoform.instrument.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AnswerKeyRequest(@NotNull Long ruleId, Long correctOptionId, String expectedText,
        BigDecimal expectedNumber, BigDecimal numericTolerance, BigDecimal score, Boolean requiresManualReview) {
}