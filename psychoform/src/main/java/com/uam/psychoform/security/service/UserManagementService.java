package com.uam.psychoform.security.service;

import com.uam.psychoform.security.model.*;
import com.uam.psychoform.security.repository.*;
import com.uam.psychoform.security.SecurityPermissions;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserManagementService {
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PermissionRepository permisos;
    private final UsuarioRolRepository usuarioRoles;
    private final RolPermisoRepository rolPermisos;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public UserManagementService(UsuarioRepository usuarios, RolRepository roles, PermissionRepository permisos,
            UsuarioRolRepository usuarioRoles, RolPermisoRepository rolPermisos, PasswordEncoder passwordEncoder,
            Clock clock) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.permisos = permisos;
        this.usuarioRoles = usuarioRoles;
        this.rolPermisos = rolPermisos;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @PreAuthorize(SecurityPermissions.USUARIO_LEER)
    public List<Usuario> listUsers() {
        return usuarios.findAll();
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.USUARIO_CREAR)
    public Usuario createUser(CreateUserCommand command) {
        LocalDateTime now = LocalDateTime.now(clock);
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNombreUsuario(command.username());
        usuario.setCorreo(command.email());
        usuario.setNombreCompleto(command.fullName());
        usuario.setHashContrasena(passwordEncoder.encode(command.password()));
        usuario.setEstado(command.status() == null ? EstadoGeneral.ACTIVO : command.status());
        usuario.setCreadoEn(now);
        usuario.setActualizadoEn(now);
        return usuarios.save(usuario);
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.USUARIO_MODIFICAR)
    public Usuario updateStatus(UUID id, EstadoGeneral status) {
        Usuario usuario = usuarios.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + id));
        usuario.setEstado(status);
        usuario.setActualizadoEn(LocalDateTime.now(clock));
        return usuarios.save(usuario);
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.USUARIO_MODIFICAR)
    public void assignRole(UUID userId, Short roleId) {
        Usuario usuario = usuarios.findById(userId).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + userId));
        Rol rol = roles.findById(roleId).orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + roleId));
        usuarioRoles.save(new UsuarioRol(usuario, rol));
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.USUARIO_MODIFICAR)
    public void removeRole(UUID userId, Short roleId) {
        usuarioRoles.deleteById(new UsuarioRolId(userId, roleId));
    }

    @PreAuthorize(SecurityPermissions.ROL_LEER)
    public List<Rol> listRoles() {
        return roles.findAll();
    }

    @PreAuthorize(SecurityPermissions.ROL_LEER)
    public List<Permiso> listPermissions() {
        return permisos.findAll();
    }

    @PreAuthorize(SecurityPermissions.ROL_LEER)
    public Set<Short> listRolePermissionIds(Short roleId) {
        if (!roles.existsById(roleId)) {
            throw new EntityNotFoundException("Rol no encontrado: " + roleId);
        }
        return rolPermisos.findPermissionIdsByRoleId(roleId);
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.ROL_MODIFICAR)
    public void replaceRolePermissions(Short roleId, Set<Short> permissionIds) {
        Rol rol = roles.findById(roleId).orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + roleId));
        for (Permiso permiso : permisos.findAll()) {
            RolPermisoId id = new RolPermisoId(roleId, permiso.getId());
            if (permissionIds.contains(permiso.getId())) {
                rolPermisos.save(new RolPermiso(rol, permiso));
            } else if (rolPermisos.existsById(id)) {
                rolPermisos.deleteById(id);
            }
        }
    }

    public record CreateUserCommand(String username, String email, String fullName, String password,
            EstadoGeneral status) {
    }
}
