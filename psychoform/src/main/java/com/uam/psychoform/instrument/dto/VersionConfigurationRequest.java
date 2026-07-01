package com.uam.psychoform.instrument.dto;

import com.uam.psychoform.instrument.model.TipoItem;
import com.uam.psychoform.instrument.model.TipoRespuesta;
import java.math.BigDecimal;
import java.util.List;

public record VersionConfigurationRequest(List<SubtestDraft> subtests) {
    public enum DraftStatus {
        EXISTING,
        CREATED,
        UPDATED,
        DELETED
    }

    public record SubtestDraft(Long id, String draftId, Long sourceId, DraftStatus status, String code,
            String name, String description, String instructions, Integer order, Integer timeLimitSeconds,
            Boolean randomizeItems, Boolean randomizeOptions, Boolean required, Short strategyId,
            List<ItemDraft> items) {
    }

    public record ItemDraft(Long id, String draftId, Long sourceId, DraftStatus status, String code,
            TipoItem itemType, TipoRespuesta responseType, String prompt, String instruction, Integer order,
            BigDecimal baseScore, Integer timeLimitSeconds, Boolean required, Boolean confidential,
            List<ImageDraft> images, List<OptionDraft> options, AnswerKeyDraft answerKey) {
    }

    public record OptionDraft(Long id, String draftId, Long sourceId, DraftStatus status, String code,
            String text, Integer order, BigDecimal ordinalValue, List<ImageDraft> images) {
    }

    public record ImageDraft(Long id, String draftId, Long sourceId, Long resourceId, DraftStatus status,
            Integer order, String altText, String role) {
    }

    public record AnswerKeyDraft(Long id, String draftId, Long sourceId, DraftStatus status, Long ruleId,
            String correctOptionDraftId, Long correctOptionId, String expectedText, BigDecimal expectedNumber,
            BigDecimal numericTolerance, BigDecimal score, Boolean requiresManualReview) {
    }
}
