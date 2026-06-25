package com.uam.psychoform.security.controller;

import com.uam.psychoform.security.dto.*;

import com.uam.psychoform.security.model.EstadoGeneral;
import com.uam.psychoform.security.service.UserManagementService;
import com.uam.psychoform.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserManagementService service;

    public UserController(UserManagementService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.ok(service.listUsers().stream().map(UserDto::from).toList());
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.ok(UserDto.from(service.createUser(new UserManagementService.CreateUserCommand(
                request.username(), request.email(), request.fullName(), request.password(), EstadoGeneral.ACTIVO))));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<?> status(@PathVariable UUID id, @Valid @RequestBody StatusRequest request) {
        return ApiResponse.ok(UserDto.from(service.updateStatus(id, request.status())));
    }

    @PostMapping("/{id}/roles")
    public ApiResponse<Void> assignRole(@PathVariable UUID id, @Valid @RequestBody RoleRequest request) {
        service.assignRole(id, request.roleId());
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    public ApiResponse<Void> removeRole(@PathVariable UUID id, @PathVariable Short roleId) {
        service.removeRole(id, roleId);
        return ApiResponse.ok(null);
    }
}


