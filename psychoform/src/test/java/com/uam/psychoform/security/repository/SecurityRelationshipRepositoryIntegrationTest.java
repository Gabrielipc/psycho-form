package com.uam.psychoform.security.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.uam.psychoform.PsychoformApplication;
import com.uam.psychoform.security.model.EstadoGeneral;
import com.uam.psychoform.security.model.Permiso;
import com.uam.psychoform.security.model.Rol;
import com.uam.psychoform.security.model.RolPermiso;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.model.UsuarioRol;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = PsychoformApplication.class)
@Transactional
class SecurityRelationshipRepositoryIntegrationTest {
    @Autowired private UsuarioRepository usuarios;
    @Autowired private RolRepository roles;
    @Autowired private PermissionRepository permisos;
    @Autowired private UsuarioRolRepository usuarioRoles;
    @Autowired private RolPermisoRepository rolPermisos;

    @Test
    void componePermisosDeMultiplesRolesYExcluyeUsuariosInactivos() {
        Usuario activo = usuarios.save(usuario("security-active-" + UUID.randomUUID(), EstadoGeneral.ACTIVO));
        Usuario inactivo = usuarios.save(usuario("security-inactive-" + UUID.randomUUID(), EstadoGeneral.INACTIVO));
        Rol rolA = roles.save(rol("SECURITY_ROLE_A_" + UUID.randomUUID()));
        Rol rolB = roles.save(rol("SECURITY_ROLE_B_" + UUID.randomUUID()));
        Permiso permisoA = permisos.save(permiso("SECURITY_PERMISSION_A_" + UUID.randomUUID()));
        Permiso permisoB = permisos.save(permiso("SECURITY_PERMISSION_B_" + UUID.randomUUID()));
        usuarioRoles.save(new UsuarioRol(activo, rolA));
        usuarioRoles.save(new UsuarioRol(activo, rolB));
        rolPermisos.save(new RolPermiso(rolA, permisoA));
        rolPermisos.save(new RolPermiso(rolB, permisoB));

        assertThat(rolPermisos.findEffectivePermissionCodesByUsuarioId(activo.getId()))
                .containsExactlyInAnyOrder(permisoA.getCodigo(), permisoB.getCodigo());
        assertThat(usuarioRoles.findRoleNamesByUsuarioId(activo.getId()))
                .containsExactlyInAnyOrder(rolA.getNombre(), rolB.getNombre());
        assertThat(usuarios.findByNombreUsuarioAndEstado(inactivo.getNombreUsuario(), EstadoGeneral.ACTIVO)).isEmpty();
    }

    private static Usuario usuario(String nombre, EstadoGeneral estado) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID()); usuario.setNombreUsuario(nombre); usuario.setCorreo(nombre + "@example.test");
        usuario.setNombreCompleto(nombre); usuario.setHashContrasena("hash"); usuario.setEstado(estado);
        usuario.setCreadoEn(LocalDateTime.now()); usuario.setActualizadoEn(LocalDateTime.now());
        return usuario;
    }
    private static Rol rol(String nombre) { Rol rol = new Rol(); rol.setNombre(nombre); return rol; }
    private static Permiso permiso(String codigo) { Permiso permiso = new Permiso(); permiso.setCodigo(codigo); return permiso; }
}
