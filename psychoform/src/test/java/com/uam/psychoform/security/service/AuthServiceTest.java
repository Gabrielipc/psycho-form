package com.uam.psychoform.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.uam.psychoform.security.model.EstadoGeneral;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import java.time.Clock;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthServiceTest {
    private final UsuarioRepository usuarios = Mockito.mock(UsuarioRepository.class);
    private final SecurityRelationshipService relaciones = Mockito.mock(SecurityRelationshipService.class);
    private final JwtService jwt = new JwtService("01234567890123456789012345678901", Clock.systemUTC());
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();
    private final AuthService service = new AuthService(usuarios, relaciones, passwords, jwt, Clock.systemUTC());

    @Test
    void credencialesValidasEmitenTokenBearerConPermisosEfectivos() {
        Usuario usuario = usuarioActivo();
        when(usuarios.findByNombreUsuarioAndEstado("ana", EstadoGeneral.ACTIVO)).thenReturn(Optional.of(usuario));
        when(relaciones.permisosEfectivos(usuario.getId())).thenReturn(Set.of("TEST_CREAR"));
        when(relaciones.rolesDeUsuario(usuario.getId())).thenReturn(Set.of("PSICOLOGO_COORDINADOR"));

        AuthService.LoginResult result = service.login("ana", "secreta");

        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.userId()).isEqualTo(usuario.getId());
        assertThat(jwt.validar(result.accessToken()).permisos()).containsExactly("TEST_CREAR");
    }

    @Test
    void credencialesInvalidasYUsuarioInactivoFallaConElMismoError() {
        Usuario activo = usuarioActivo();
        when(usuarios.findByNombreUsuarioAndEstado("ana", EstadoGeneral.ACTIVO)).thenReturn(Optional.of(activo));
        assertThatThrownBy(() -> service.login("ana", "incorrecta"))
                .hasMessage("Credenciales inválidas");

        when(usuarios.findByNombreUsuarioAndEstado("inactivo", EstadoGeneral.ACTIVO)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.login("inactivo", "secreta"))
                .hasMessage("Credenciales inválidas");
    }

    private Usuario usuarioActivo() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNombreUsuario("ana");
        usuario.setEstado(EstadoGeneral.ACTIVO);
        usuario.setHashContrasena(passwords.encode("secreta"));
        return usuario;
    }
}
