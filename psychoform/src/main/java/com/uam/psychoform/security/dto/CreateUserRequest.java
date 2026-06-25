package com.uam.psychoform.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(@NotBlank String username, @Email @NotBlank String email,
        @NotBlank String fullName, @NotBlank String password) {
}