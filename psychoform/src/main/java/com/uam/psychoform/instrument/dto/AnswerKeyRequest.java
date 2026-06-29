package com.uam.psychoform.instrument.dto;

import java.math.BigDecimal;

public record AnswerKeyRequest(Long ruleId, Long correctOptionId, String expectedText,
        BigDecimal expectedNumber, BigDecimal numericTolerance, BigDecimal score, Boolean requiresManualReview) {
}
