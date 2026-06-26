package com.uam.psychoform.instrument.controller;

import com.uam.psychoform.instrument.dto.*;

import com.uam.psychoform.instrument.model.*;
import com.uam.psychoform.instrument.service.*;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.dto.EntityView;
import com.uam.psychoform.security.SecurityPermissions;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class InstrumentController {
    private final InstrumentAdminService admin;
    private final VersionTestService versions;

    public InstrumentController(InstrumentAdminService admin, VersionTestService versions) {
        this.admin = admin;
        this.versions = versions;
    }

    @GetMapping("/tests")
    @PreAuthorize(SecurityPermissions.TEST_LEER)
    public ApiResponse<?> tests() {
        return ApiResponse.ok(EntityView.of(admin.listTests()));
    }

    @PostMapping("/tests")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> createTest(@Valid @RequestBody TestRequest request) {
        return ApiResponse.ok(EntityView.of(admin.createTest(new InstrumentAdminService.TestCommand(request.code(), request.name(),
                request.description()))));
    }

    @GetMapping("/tests/{id}/versions")
    @PreAuthorize(SecurityPermissions.TEST_LEER)
    public ApiResponse<?> versions(@PathVariable Long id) {
        return ApiResponse.ok(EntityView.of(admin.listVersions(id)));
    }

    @PostMapping("/tests/{id}/versions")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> createVersion(@PathVariable Long id, @Valid @RequestBody VersionRequest request) {
        return ApiResponse.ok(EntityView.of(admin.createVersion(id, toCommand(request))));
    }

    @PatchMapping("/test-versions/{id}")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> updateVersion(@PathVariable Long id, @Valid @RequestBody VersionRequest request) {
        return ApiResponse.ok(EntityView.of(admin.updateVersion(id, toCommand(request))));
    }

    @PostMapping("/test-versions/{id}/approve")
    @PreAuthorize(SecurityPermissions.TEST_PUBLICAR)
    public ApiResponse<?> approve(@PathVariable Long id) {
        return ApiResponse.ok(EntityView.of(versions.approveVersion(id)));
    }

    @PostMapping("/test-versions/{id}/publish")
    @PreAuthorize(SecurityPermissions.TEST_PUBLICAR)
    public ApiResponse<?> publish(@PathVariable Long id) {
        return ApiResponse.ok(EntityView.of(versions.publishVersion(id)));
    }

    @GetMapping("/test-versions/{id}/subtests")
    @PreAuthorize(SecurityPermissions.TEST_LEER)
    public ApiResponse<?> getSubtests(@PathVariable Long id) {
        return ApiResponse.ok(EntityView.of(admin.listSubtests(id)));
    }

    @PostMapping("/test-versions/{id}/subtests")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> subtest(@PathVariable Long id, @Valid @RequestBody SubtestRequest request) {
        return ApiResponse.ok(EntityView.of(admin.createSubtest(id,
                new InstrumentAdminService.SubtestCommand(request.code(), request.name(), request.description(),
                        request.instructions(), request.order(), request.timeLimitSeconds(), request.randomizeItems(),
                        request.randomizeOptions(), request.required(), request.strategyId()))));
    }

    @PostMapping("/subtests/{id}/items")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> item(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return ApiResponse.ok(EntityView.of(admin.createItem(id,
                new InstrumentAdminService.ItemCommand(request.code(), request.itemType(), request.responseType(),
                        request.prompt(), request.instruction(), request.order(), request.baseScore(),
                        request.timeLimitSeconds(), request.required(), request.confidential()))));
    }

    @PostMapping("/items/{id}/options")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> option(@PathVariable Long id, @Valid @RequestBody OptionRequest request) {
        return ApiResponse.ok(EntityView.of(admin.createOption(id, new InstrumentAdminService.OptionCommand(request.code(),
                request.text(), request.order(), request.ordinalValue()))));
    }

    @GetMapping("/scoring/strategies")
    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR)
    public ApiResponse<?> strategies() {
        return ApiResponse.ok(EntityView.of(admin.listStrategies()));
    }

    @PostMapping("/subtests/{id}/scoring-rules")
    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR)
    public ApiResponse<?> scoringRule(@PathVariable Long id, @Valid @RequestBody ScoringRuleRequest request) {
        return ApiResponse.ok(EntityView.of(admin.createScoringRule(id,
                new InstrumentAdminService.ScoringRuleCommand(request.strategyId(), request.ruleType(), request.itemId(),
                        request.priority(), request.parametersJson(), request.observation()))));
    }

    @PostMapping("/items/{id}/answer-key")
    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR)
    public ApiResponse<?> answerKey(@PathVariable Long id, @Valid @RequestBody AnswerKeyRequest request) {
        return ApiResponse.ok(EntityView.of(admin.createAnswerKey(id,
                new InstrumentAdminService.AnswerKeyCommand(request.ruleId(), request.correctOptionId(),
                        request.expectedText(), request.expectedNumber(), request.numericTolerance(), request.score(),
                        request.requiresManualReview()))));
    }

    @PostMapping("/baremos")
    @PreAuthorize(SecurityPermissions.BAREMO_CONFIGURAR)
    public ApiResponse<?> baremo(@Valid @RequestBody BaremoRequest request) {
        return ApiResponse.ok(EntityView.of(admin.createBaremo(new InstrumentAdminService.BaremoCommand(
                request.versionId(), request.dimensionId(), request.code(), request.name(), request.description(),
                request.normativeGroup()))));
    }

    @PostMapping("/baremos/{id}/ranges")
    @PreAuthorize(SecurityPermissions.BAREMO_CONFIGURAR)
    public ApiResponse<?> baremoRange(@PathVariable Long id, @Valid @RequestBody BaremoRangeRequest request) {
        return ApiResponse.ok(EntityView.of(admin.createBaremoRange(id,
                new InstrumentAdminService.BaremoRangeCommand(request.minScore(), request.maxScore(),
                        request.percentile(), request.category(), request.interpretation(), request.recommendation(),
                        request.order()))));
    }

    private InstrumentAdminService.VersionCommand toCommand(VersionRequest request) {
        return new InstrumentAdminService.VersionCommand(request.number(), request.strategyId(), request.instructions(),
                request.timeLimitSeconds(), request.randomizeSubtests(), request.randomizeItems());
    }
}
