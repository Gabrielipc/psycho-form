package com.uam.psychoform.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.uam.psychoform.security.entity.Rol;
import com.uam.psychoform.security.repository.RolRepository;
import com.uam.psychoform.security.repository.UsuarioRepository;
import com.uam.psychoform.security.repository.UsuarioRolRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BootstrapAdminTest {
    @Test
    void creaUnSoloAdministradorConHashBcryptYSuRol() throws Exception {
        UsuarioRepository usuarios = Mockito.mock(UsuarioRepository.class);
        RolRepository roles = Mockito.mock(RolRepository.class);
        UsuarioRolRepository usuarioRoles = Mockito.mock(UsuarioRolRepository.class);
        Rol admin = new Rol(); admin.setId((short) 1); admin.setNombre("ADMINISTRADOR");
        when(usuarios.count()).thenReturn(0L); when(roles.findByNombre("ADMINISTRADOR")).thenReturn(Optional.of(admin));
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        MockEnvironment env = new MockEnvironment().withProperty("BFA_BOOTSTRAP_ADMIN_USERNAME", "admin")
                .withProperty("BFA_BOOTSTRAP_ADMIN_EMAIL", "admin@example.test")
                .withProperty("BFA_BOOTSTRAP_ADMIN_PASSWORD", "secreta");

        new BootstrapAdmin().bootstrapAdminRunner(usuarios, roles, usuarioRoles, encoder, env).run();

        ArgumentCaptor<com.uam.psychoform.security.entity.Usuario> captor = ArgumentCaptor.forClass(com.uam.psychoform.security.entity.Usuario.class);
        verify(usuarios).save(captor.capture()); verify(usuarioRoles).save(Mockito.any());
        assertThat(encoder.matches("secreta", captor.getValue().getHashContrasena())).isTrue();
    }

    @Test
    void noCreaOtroUsuarioCuandoYaExisteUno() throws Exception {
        UsuarioRepository usuarios = Mockito.mock(UsuarioRepository.class);
        RolRepository roles = Mockito.mock(RolRepository.class);
        UsuarioRolRepository usuarioRoles = Mockito.mock(UsuarioRolRepository.class);
        when(usuarios.count()).thenReturn(1L);
        new BootstrapAdmin().bootstrapAdminRunner(usuarios, roles, usuarioRoles, new BCryptPasswordEncoder(), new MockEnvironment()).run();
        verifyNoInteractions(roles, usuarioRoles);
        verify(usuarios).count();
        Mockito.verifyNoMoreInteractions(usuarios);
    }
}
