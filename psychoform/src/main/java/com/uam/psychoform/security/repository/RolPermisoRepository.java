package com.uam.psychoform.security.repository;

import com.uam.psychoform.security.model.Permiso;
import com.uam.psychoform.security.model.RolPermiso;
import com.uam.psychoform.security.model.RolPermisoId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, RolPermisoId> {
    @Query("select distinct rolPermiso.permiso.codigo from RolPermiso rolPermiso join UsuarioRol usuarioRol on usuarioRol.rol.id = rolPermiso.rol.id where usuarioRol.usuario.id = :usuarioId")
    Set<String> findEffectivePermissionCodesByUsuarioId(UUID usuarioId);

    @Query("""
            select new com.uam.psychoform.security.repository.UserPermissionCode(
                usuarioRol.usuario.id,
                rolPermiso.permiso.codigo
            )
            from RolPermiso rolPermiso
            join UsuarioRol usuarioRol on usuarioRol.rol.id = rolPermiso.rol.id
            """)
    List<UserPermissionCode> findAllEffectivePermissionCodesByUsuario();

    @Query("SELECT rp.permiso FROM RolPermiso rp WHERE rp.rol.id = :roleId")
    List<Permiso> findPermisosByRolId(@Param("roleId") Short roleId);
}
