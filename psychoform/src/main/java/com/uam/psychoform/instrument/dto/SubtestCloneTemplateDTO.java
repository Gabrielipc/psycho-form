package com.uam.psychoform.instrument.dto;

import com.uam.psychoform.instrument.model.TipoItem;
import com.uam.psychoform.instrument.model.TipoRespuesta;
import com.uam.psychoform.security.model.EstadoGeneral;
import java.math.BigDecimal;
import java.util.List;

public record SubtestCloneTemplateDTO(Long sourceSubtestId, String code, String name, String description,
        String instructions, Integer order, Integer timeLimitSeconds, Boolean randomizeItems,
        Boolean randomizeOptions, Boolean required, Short strategyId, EstadoGeneral estado,
        List<ItemTemplate> items) {

    public record ItemTemplate(Long sourceItemId, String code, TipoItem itemType, TipoRespuesta responseType,
            String prompt, String instruction, Integer order, BigDecimal baseScore, Integer timeLimitSeconds,
            Boolean required, Boolean confidential, EstadoGeneral estado, List<ImageTemplate> images,
            List<OptionTemplate> options, AnswerKeyTemplate answerKey) {
    }

    public record OptionTemplate(Long sourceOptionId, String code, String text, Integer order,
            BigDecimal ordinalValue, EstadoGeneral estado, List<ImageTemplate> images) {
    }

    public record ImageTemplate(Long sourceImageId, Long resourceId, Integer order, String altText,
            String storagePath, String role) {
    }

    public record AnswerKeyTemplate(Long sourceAnswerKeyId, Long sourceRuleId, Long correctOptionSourceId,
            String expectedText, BigDecimal expectedNumber, BigDecimal numericTolerance, BigDecimal score,
            Boolean requiresManualReview) {
    }
}
