package com.uam.psychoform.security.service;

import com.uam.psychoform.security.repository.RolPermisoRepository;
import com.uam.psychoform.security.repository.UsuarioRolRepository;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SecurityRelationshipService {
    private final UsuarioRolRepository usuarioRoles;
    private final RolPermisoRepository rolPermisos;

    public SecurityRelationshipService(UsuarioRolRepository usuarioRoles, RolPermisoRepository rolPermisos) {
        this.usuarioRoles = usuarioRoles;
        this.rolPermisos = rolPermisos;
    }

    public Set<String> rolesDeUsuario(UUID usuarioId) {
        return usuarioRoles.findRoleNamesByUsuarioId(usuarioId);
    }

    public Set<String> permisosEfectivos(UUID usuarioId) {
        return rolPermisos.findEffectivePermissionCodesByUsuarioId(usuarioId);
    }
}
