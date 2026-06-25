package com.uam.psychoform.security.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record PermissionsRequest(@NotNull Set<Short> permissionIds) {
}