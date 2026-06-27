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
    private final InstrumentImageService images;

    public InstrumentController(InstrumentAdminService admin, VersionTestService versions, InstrumentImageService images) {
        this.admin = admin;
        this.versions = versions;
        this.images = images;
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

    @GetMapping("/subtests/{id}/items")
    @PreAuthorize(SecurityPermissions.TEST_LEER)
    public ApiResponse<?> getItems(@PathVariable Long id) {
        return ApiResponse.ok(admin.listItemDtos(id));
    }

    @GetMapping("/items/{id}/options")
    @PreAuthorize(SecurityPermissions.TEST_LEER)
    public ApiResponse<?> getOptions(@PathVariable Long id) {
        return ApiResponse.ok(admin.listOptionDtos(id));
    }

    @PostMapping("/test-versions/{id}/subtests")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> subtest(@PathVariable Long id, @Valid @RequestBody SubtestRequest request) {
        return ApiResponse.ok(EntityView.of(admin.createSubtest(id,
                new InstrumentAdminService.SubtestCommand(request.code(), request.name(), request.description(),
                        request.instructions(), request.order(), request.timeLimitSeconds(), request.randomizeItems(),
                        request.randomizeOptions(), request.required(), request.strategyId()))));
    }

    @PatchMapping("/subtests/{id}")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> updateSubtest(@PathVariable Long id, @Valid @RequestBody SubtestRequest request) {
        return ApiResponse.ok(EntityView.of(admin.updateSubtest(id, toCommand(request))));
    }

    @PostMapping("/subtests/{id}/items")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> item(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return ApiResponse.ok(EntityView.of(admin.createItem(id,
                new InstrumentAdminService.ItemCommand(request.code(), request.itemType(), request.responseType(),
                        request.prompt(), request.instruction(), request.order(), request.baseScore(),
                        request.timeLimitSeconds(), request.required(), request.confidential()))));
    }

    @PatchMapping("/items/{id}")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> updateItem(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return ApiResponse.ok(EntityView.of(admin.updateItem(id, toCommand(request))));
    }

    @PostMapping("/items/{id}/options")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> option(@PathVariable Long id, @Valid @RequestBody OptionRequest request) {
        return ApiResponse.ok(EntityView.of(admin.createOption(id, new InstrumentAdminService.OptionCommand(request.code(),
                request.text(), request.order(), request.ordinalValue()))));
    }

    @PatchMapping("/options/{id}")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> updateOption(@PathVariable Long id, @Valid @RequestBody OptionRequest request) {
        return ApiResponse.ok(EntityView.of(admin.updateOption(id, toCommand(request))));
    }

    @PostMapping(value = "/items/{id}/images", consumes = "multipart/form-data")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> itemImage(@PathVariable Long id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false) Integer order, @RequestParam(required = false) String altText,
            @RequestParam(required = false, defaultValue = "ENUNCIADO") String role) throws java.io.IOException {
        return ApiResponse.ok(EntityView.of(images.uploadItemImage(id, file, order, altText, role)));
    }

    @PostMapping(value = "/options/{id}/images", consumes = "multipart/form-data")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> optionImage(@PathVariable Long id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false) Integer order, @RequestParam(required = false) String altText)
            throws java.io.IOException {
        return ApiResponse.ok(EntityView.of(images.uploadOptionImage(id, file, order, altText)));
    }

    @PutMapping(value = "/items/{itemId}/images/{imageId}", consumes = "multipart/form-data")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> replaceItemImage(@PathVariable Long itemId, @PathVariable Long imageId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false) String altText,
            @RequestParam(required = false, defaultValue = "ENUNCIADO") String role) throws java.io.IOException {
        return ApiResponse.ok(EntityView.of(images.replaceItemImage(itemId, imageId, file, altText, role)));
    }

    @PutMapping(value = "/items/{itemId}/images/by-order/{order}", consumes = "multipart/form-data")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> replaceItemImageByOrder(@PathVariable Long itemId, @PathVariable Integer order,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false) String altText,
            @RequestParam(required = false, defaultValue = "ENUNCIADO") String role) throws java.io.IOException {
        return ApiResponse.ok(EntityView.of(images.replaceItemImageByOrder(itemId, order, file, altText, role)));
    }

    @PutMapping(value = "/options/{optionId}/images/{imageId}", consumes = "multipart/form-data")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> replaceOptionImage(@PathVariable Long optionId, @PathVariable Long imageId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false) String altText) throws java.io.IOException {
        return ApiResponse.ok(EntityView.of(images.replaceOptionImage(optionId, imageId, file, altText)));
    }

    @PutMapping(value = "/options/{optionId}/images/by-order/{order}", consumes = "multipart/form-data")
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ApiResponse<?> replaceOptionImageByOrder(@PathVariable Long optionId, @PathVariable Integer order,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false) String altText) throws java.io.IOException {
        return ApiResponse.ok(EntityView.of(images.replaceOptionImageByOrder(optionId, order, file, altText)));
    }

    @GetMapping("/scoring/strategies")
    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR)
    public ApiResponse<?> strategies() {
        return ApiResponse.ok(EntityView.of(admin.listStrategies()));
    }

    @GetMapping("/subtests/{id}/scoring-rules")
    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR + " or " + SecurityPermissions.TEST_LEER)
    public ApiResponse<?> scoringRules(@PathVariable Long id) {
        return ApiResponse.ok(EntityView.of(admin.listScoringRules(id)));
    }

    @PostMapping("/subtests/{id}/scoring-rules")
    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR)
    public ApiResponse<?> scoringRule(@PathVariable Long id, @Valid @RequestBody ScoringRuleRequest request) {
        return ApiResponse.ok(EntityView.of(admin.createScoringRule(id,
                new InstrumentAdminService.ScoringRuleCommand(request.strategyId(), request.ruleType(), request.itemId(),
                        request.priority(), request.parametersJson(), request.observation()))));
    }

    @PatchMapping("/scoring-rules/{id}")
    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR)
    public ApiResponse<?> updateScoringRule(@PathVariable Long id, @Valid @RequestBody ScoringRuleRequest request) {
        return ApiResponse.ok(EntityView.of(admin.updateScoringRule(id, toCommand(request))));
    }

    @GetMapping("/items/{id}/answer-key")
    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR + " or " + SecurityPermissions.TEST_LEER)
    public ApiResponse<?> getAnswerKey(@PathVariable Long id) {
        return ApiResponse.ok(EntityView.of(admin.findAnswerKey(id)));
    }

    @PostMapping("/items/{id}/answer-key")
    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR)
    public ApiResponse<?> answerKey(@PathVariable Long id, @Valid @RequestBody AnswerKeyRequest request) {
        return ApiResponse.ok(EntityView.of(admin.createAnswerKey(id,
                new InstrumentAdminService.AnswerKeyCommand(request.ruleId(), request.correctOptionId(),
                        request.expectedText(), request.expectedNumber(), request.numericTolerance(), request.score(),
                        request.requiresManualReview()))));
    }

    @PatchMapping("/answer-keys/{id}")
    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR)
    public ApiResponse<?> updateAnswerKey(@PathVariable Long id, @Valid @RequestBody AnswerKeyRequest request) {
        return ApiResponse.ok(EntityView.of(admin.updateAnswerKey(id, toCommand(request))));
    }

    @GetMapping("/test-versions/{id}/baremos")
    @PreAuthorize(SecurityPermissions.BAREMO_CONFIGURAR + " or " + SecurityPermissions.TEST_LEER)
    public ApiResponse<?> getBaremos(@PathVariable Long id) {
        return ApiResponse.ok(EntityView.of(admin.listBaremos(id)));
    }

    @PostMapping("/baremos")
    @PreAuthorize(SecurityPermissions.BAREMO_CONFIGURAR)
    public ApiResponse<?> baremo(@Valid @RequestBody BaremoRequest request) {
        return ApiResponse.ok(EntityView.of(admin.createBaremo(new InstrumentAdminService.BaremoCommand(
                request.versionId(), request.dimensionId(), request.code(), request.name(), request.description(),
                request.normativeGroup()))));
    }

    @PatchMapping("/baremos/{id}")
    @PreAuthorize(SecurityPermissions.BAREMO_CONFIGURAR)
    public ApiResponse<?> updateBaremo(@PathVariable Long id, @Valid @RequestBody BaremoRequest request) {
        return ApiResponse.ok(EntityView.of(admin.updateBaremo(id, toCommand(request))));
    }

    @GetMapping("/baremos/{id}/ranges")
    @PreAuthorize(SecurityPermissions.BAREMO_CONFIGURAR + " or " + SecurityPermissions.TEST_LEER)
    public ApiResponse<?> getBaremoRanges(@PathVariable Long id) {
        return ApiResponse.ok(EntityView.of(admin.listBaremoRanges(id)));
    }

    @PostMapping("/baremos/{id}/ranges")
    @PreAuthorize(SecurityPermissions.BAREMO_CONFIGURAR)
    public ApiResponse<?> baremoRange(@PathVariable Long id, @Valid @RequestBody BaremoRangeRequest request) {
        return ApiResponse.ok(EntityView.of(admin.createBaremoRange(id,
                new InstrumentAdminService.BaremoRangeCommand(request.minScore(), request.maxScore(),
                        request.percentile(), request.category(), request.interpretation(), request.recommendation(),
                        request.order()))));
    }

    @PatchMapping("/baremo-ranges/{id}")
    @PreAuthorize(SecurityPermissions.BAREMO_CONFIGURAR)
    public ApiResponse<?> updateBaremoRange(@PathVariable Long id, @Valid @RequestBody BaremoRangeRequest request) {
        return ApiResponse.ok(EntityView.of(admin.updateBaremoRange(id, toCommand(request))));
    }

    private InstrumentAdminService.VersionCommand toCommand(VersionRequest request) {
        return new InstrumentAdminService.VersionCommand(request.number(), request.strategyId(), request.instructions(),
                request.timeLimitSeconds(), request.randomizeSubtests(), request.randomizeItems());
    }

    private InstrumentAdminService.SubtestCommand toCommand(SubtestRequest request) {
        return new InstrumentAdminService.SubtestCommand(request.code(), request.name(), request.description(),
                request.instructions(), request.order(), request.timeLimitSeconds(), request.randomizeItems(),
                request.randomizeOptions(), request.required(), request.strategyId());
    }

    private InstrumentAdminService.ItemCommand toCommand(ItemRequest request) {
        return new InstrumentAdminService.ItemCommand(request.code(), request.itemType(), request.responseType(),
                request.prompt(), request.instruction(), request.order(), request.baseScore(),
                request.timeLimitSeconds(), request.required(), request.confidential());
    }

    private InstrumentAdminService.OptionCommand toCommand(OptionRequest request) {
        return new InstrumentAdminService.OptionCommand(request.code(), request.text(), request.order(),
                request.ordinalValue());
    }

    private InstrumentAdminService.ScoringRuleCommand toCommand(ScoringRuleRequest request) {
        return new InstrumentAdminService.ScoringRuleCommand(request.strategyId(), request.ruleType(), request.itemId(),
                request.priority(), request.parametersJson(), request.observation());
    }

    private InstrumentAdminService.AnswerKeyCommand toCommand(AnswerKeyRequest request) {
        return new InstrumentAdminService.AnswerKeyCommand(request.ruleId(), request.correctOptionId(),
                request.expectedText(), request.expectedNumber(), request.numericTolerance(), request.score(),
                request.requiresManualReview());
    }

    private InstrumentAdminService.BaremoCommand toCommand(BaremoRequest request) {
        return new InstrumentAdminService.BaremoCommand(request.versionId(), request.dimensionId(), request.code(),
                request.name(), request.description(), request.normativeGroup());
    }

    private InstrumentAdminService.BaremoRangeCommand toCommand(BaremoRangeRequest request) {
        return new InstrumentAdminService.BaremoRangeCommand(request.minScore(), request.maxScore(),
                request.percentile(), request.category(), request.interpretation(), request.recommendation(),
                request.order());
    }
}
