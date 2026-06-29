package com.uam.psychoform.security.controller;

import com.uam.psychoform.security.dto.*;

import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.SecurityPermissions;
import com.uam.psychoform.security.service.AuthService;
import com.uam.psychoform.security.service.PasswordResetRequestService;
import com.uam.psychoform.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;
    private final CurrentActor currentActor;
    private final PasswordResetRequestService passwordResetRequests;

    public AuthController(AuthService auth, CurrentActor currentActor, PasswordResetRequestService passwordResetRequests) {
        this.auth = auth;
        this.currentActor = currentActor;
        this.passwordResetRequests = passwordResetRequests;
    }

    @PostMapping("/login")
    public ApiResponse<AuthService.LoginResult> login(@Valid @RequestBody LoginRequest r) {
        return ApiResponse.ok(auth.login(r.username(), r.password()));
    }

    @GetMapping("/me")
    @PreAuthorize(SecurityPermissions.AUTHENTICATED)
    public ApiResponse<MeResponse> me() {
        var principal = currentActor.principal();
        return ApiResponse.ok(new MeResponse(principal.usuarioId(), principal.username(), principal.permisos(),
                principal.roles()));
    }

    @PostMapping("/password-reset")
    @PreAuthorize("permitAll()")
    public ApiResponse<?> passwordReset(@Valid @RequestBody PasswordResetRequest request) {
        return ApiResponse.ok(passwordResetRequests.request(request.usernameOrEmail()));
    }

    public record PasswordResetRequest(@NotBlank String usernameOrEmail) {
    }
}
