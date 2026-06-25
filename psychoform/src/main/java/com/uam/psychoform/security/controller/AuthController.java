package com.uam.psychoform.security.controller;

import com.uam.psychoform.security.dto.*;

import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.service.AuthService;
import com.uam.psychoform.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;
    private final CurrentActor currentActor;

    public AuthController(AuthService auth, CurrentActor currentActor) {
        this.auth = auth;
        this.currentActor = currentActor;
    }

    @PostMapping("/login")
    public ApiResponse<AuthService.LoginResult> login(@Valid @RequestBody LoginRequest r) {
        return ApiResponse.ok(auth.login(r.username(), r.password()));
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me() {
        var principal = currentActor.principal();
        return ApiResponse.ok(new MeResponse(principal.usuarioId(), principal.username(), principal.permisos(),
                principal.roles()));
    }
}

