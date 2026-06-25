package com.uam.psychoform.security.controller;

import com.uam.psychoform.security.dto.PermissionsRequest;

import com.uam.psychoform.security.service.UserManagementService;
import com.uam.psychoform.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class RoleController {
    private final UserManagementService service;

    public RoleController(UserManagementService service) {
        this.service = service;
    }

    @GetMapping("/roles")
    public ApiResponse<?> roles() {
        return ApiResponse.ok(service.listRoles());
    }

    @GetMapping("/permissions")
    public ApiResponse<?> permissions() {
        return ApiResponse.ok(service.listPermissions());
    }

    @PutMapping("/roles/{id}/permissions")
    public ApiResponse<Void> permissions(@PathVariable Short id, @Valid @RequestBody PermissionsRequest request) {
        service.replaceRolePermissions(id, request.permissionIds());
        return ApiResponse.ok(null);
    }
}


