package com.uam.psychoform.security.web;

import com.uam.psychoform.security.service.AuthService;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public AuthService.LoginResult login(@RequestBody LoginRequest r) {
        return auth.login(r.username(), r.password());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, Object>> denied() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("status", HttpStatus.UNAUTHORIZED.value(), "error", HttpStatus.UNAUTHORIZED.getReasonPhrase()));
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }
}
