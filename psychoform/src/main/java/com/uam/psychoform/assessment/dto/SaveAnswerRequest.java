package com.uam.psychoform.assessment.dto;

import java.math.BigDecimal;
import java.util.List;

public record SaveAnswerRequest(Long itemId, List<Long> selectedOptionIds, String textAnswer,
        BigDecimal numericAnswer, Integer timeUsedSeconds) {
    public SaveAnswerRequest {
        selectedOptionIds = selectedOptionIds == null ? List.of() : selectedOptionIds;
    }
}