package com.uam.psychoform.scoring.controller;

import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.scoring.service.ManualReviewService;
import com.uam.psychoform.security.SecurityPermissions;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manual-review")
public class ManualReviewController {
    private final ManualReviewService service;

    public ManualReviewController(ManualReviewService service) {
        this.service = service;
    }

    @GetMapping("/pending")
    @PreAuthorize(SecurityPermissions.CALIFICACION_EJECUTAR + " or " + SecurityPermissions.RESULTADO_VER)
    public ApiResponse<?> pending() {
        return ApiResponse.ok(service.pending());
    }

    @PostMapping("/answers/{answerId}/pending")
    @PreAuthorize(SecurityPermissions.CALIFICACION_EJECUTAR)
    public ApiResponse<?> createPending(@PathVariable Long answerId) {
        return ApiResponse.ok(service.createPendingForAnswer(answerId));
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize(SecurityPermissions.CALIFICACION_EJECUTAR)
    public ApiResponse<?> resolve(@PathVariable Long reviewId, @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.ok(service.resolve(reviewId,
                new ManualReviewService.ReviewCommand(request.score(), request.comment(), request.approved())));
    }

    public record ReviewRequest(BigDecimal score, String comment, boolean approved) {
    }
}
