package com.uam.psychoform.security.controller;

import com.uam.psychoform.security.dto.PermissionsRequest;

import com.uam.psychoform.security.SecurityPermissions;
import com.uam.psychoform.security.service.UserManagementService;
import com.uam.psychoform.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class RoleController {
    private final UserManagementService service;

    public RoleController(UserManagementService service) {
        this.service = service;
    }

    @GetMapping("/roles")
    @PreAuthorize(SecurityPermissions.ROL_LEER)
    public ApiResponse<?> roles() {
        return ApiResponse.ok(service.listRoles());
    }

    @GetMapping("/permissions")
    @PreAuthorize(SecurityPermissions.ROL_LEER)
    public ApiResponse<?> permissions() {
        return ApiResponse.ok(service.listPermissions());
    }

    @GetMapping("/roles/{id}/permissions")
    @PreAuthorize(SecurityPermissions.ROL_LEER)
    public ApiResponse<?> rolePermissions(@PathVariable Short id) {
        return ApiResponse.ok(service.listRolePermissionIds(id));
    }

    @PutMapping("/roles/{id}/permissions")
    @PreAuthorize(SecurityPermissions.ROL_MODIFICAR)
    public ApiResponse<Void> permissions(@PathVariable Short id, @Valid @RequestBody PermissionsRequest request) {
        service.replaceRolePermissions(id, request.permissionIds());
        return ApiResponse.ok(null);
    }
}
