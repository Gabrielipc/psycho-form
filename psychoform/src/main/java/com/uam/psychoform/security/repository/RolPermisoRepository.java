package com.uam.psychoform.security.repository;

import com.uam.psychoform.security.model.RolPermiso;
import com.uam.psychoform.security.model.RolPermisoId;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, RolPermisoId> {
    @Query("select distinct rolPermiso.permiso.codigo from RolPermiso rolPermiso join UsuarioRol usuarioRol on usuarioRol.rol.id = rolPermiso.rol.id where usuarioRol.usuario.id = :usuarioId")
    Set<String> findEffectivePermissionCodesByUsuarioId(UUID usuarioId);

    @Query("select rolPermiso.permiso.id from RolPermiso rolPermiso where rolPermiso.rol.id = :roleId")
    Set<Short> findPermissionIdsByRoleId(Short roleId);
}
