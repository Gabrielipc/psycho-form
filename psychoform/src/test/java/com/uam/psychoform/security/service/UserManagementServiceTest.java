package com.uam.psychoform.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.uam.psychoform.security.model.EstadoGeneral;
import com.uam.psychoform.security.model.Permiso;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.PermissionRepository;
import com.uam.psychoform.security.repository.RolPermisoRepository;
import com.uam.psychoform.security.repository.UserPermissionCode;
import com.uam.psychoform.security.repository.UserRoleName;
import com.uam.psychoform.security.repository.RolRepository;
import com.uam.psychoform.security.repository.UsuarioRepository;
import com.uam.psychoform.security.repository.UsuarioRolRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserManagementServiceTest {
    private final UsuarioRepository usuarios = Mockito.mock(UsuarioRepository.class);
    private final RolRepository roles = Mockito.mock(RolRepository.class);
    private final PermissionRepository permisos = Mockito.mock(PermissionRepository.class);
    private final UsuarioRolRepository usuarioRoles = Mockito.mock(UsuarioRolRepository.class);
    private final RolPermisoRepository rolPermisos = Mockito.mock(RolPermisoRepository.class);
    private final PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
    private final UserManagementService service = new UserManagementService(usuarios, roles, permisos, usuarioRoles,
            rolPermisos, passwordEncoder, Clock.systemUTC());

    @Test
    void permissionMatrixReturnsEffectivePermissionsByUserRoles() {
        UUID anaId = UUID.randomUUID();
        UUID benId = UUID.randomUUID();

        when(usuarios.findAll()).thenReturn(List.of(
                usuario(benId, "ben", "ben@uam.edu.ni", "Ben Ramos", EstadoGeneral.INACTIVO),
                usuario(anaId, "ana", "ana@uam.edu.ni", "Ana Ruiz", EstadoGeneral.ACTIVO)));
        when(permisos.findAllByOrderByCodigoAsc()).thenReturn(List.of(
                permiso((short) 2, "ROL_LEER"),
                permiso((short) 1, "USUARIO_LEER"),
                permiso((short) 3, "USUARIO_MODIFICAR")));
        when(usuarioRoles.findAllUserRoleNames()).thenReturn(List.of(
                new UserRoleName(anaId, "ADMINISTRADOR"),
                new UserRoleName(anaId, "EVALUADOR")));
        when(rolPermisos.findAllEffectivePermissionCodesByUsuario()).thenReturn(List.of(
                new UserPermissionCode(anaId, "USUARIO_LEER"),
                new UserPermissionCode(anaId, "USUARIO_MODIFICAR"),
                new UserPermissionCode(anaId, "USUARIO_LEER")));

        UserManagementService.PermissionMatrix matrix = service.permissionMatrix();

        assertThat(matrix.permissionColumns()).containsExactly("ROL_LEER", "USUARIO_LEER", "USUARIO_MODIFICAR");
        assertThat(matrix.users()).hasSize(2);

        UserManagementService.UserPermissionRow ana = matrix.users().getFirst();
        assertThat(ana.userId()).isEqualTo(anaId);
        assertThat(ana.username()).isEqualTo("ana");
        assertThat(ana.roles()).containsExactly("ADMINISTRADOR", "EVALUADOR");
        assertThat(ana.permissions()).containsExactly("USUARIO_LEER", "USUARIO_MODIFICAR");
        assertThat(ana.matrix())
                .containsEntry("ROL_LEER", false)
                .containsEntry("USUARIO_LEER", true)
                .containsEntry("USUARIO_MODIFICAR", true);

        UserManagementService.UserPermissionRow ben = matrix.users().get(1);
        assertThat(ben.userId()).isEqualTo(benId);
        assertThat(ben.roles()).isEmpty();
        assertThat(ben.permissions()).isEmpty();
        assertThat(ben.matrix()).containsEntry("USUARIO_LEER", false);
    }

    private static Usuario usuario(UUID id, String username, String email, String fullName, EstadoGeneral status) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombreUsuario(username);
        usuario.setCorreo(email);
        usuario.setNombreCompleto(fullName);
        usuario.setEstado(status);
        return usuario;
    }

    private static Permiso permiso(Short id, String code) {
        Permiso permiso = new Permiso();
        permiso.setId(id);
        permiso.setCodigo(code);
        return permiso;
    }
}
