package com.uam.psychoform.security.repository;

import java.util.UUID;

public record UserPermissionCode(UUID userId, String permissionCode) {
}
