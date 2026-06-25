package com.uam.psychoform.security.dto;

import jakarta.validation.constraints.NotNull;

public record RoleRequest(@NotNull Short roleId) {
}