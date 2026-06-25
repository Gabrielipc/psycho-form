package com.uam.psychoform.security;

import com.uam.psychoform.security.entity.*;
import com.uam.psychoform.security.repository.*;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
class BootstrapAdmin {
    @Bean
    CommandLineRunner bootstrapAdminRunner(UsuarioRepository users, RolRepository roles,
            UsuarioRolRepository usuarioRoles, PasswordEncoder encoder, Environment env) {
        return args -> {
            if (users.count() != 0)
                return;
            String u = env.getProperty("BFA_BOOTSTRAP_ADMIN_USERNAME"),
                    e = env.getProperty("BFA_BOOTSTRAP_ADMIN_EMAIL"),
                    p = env.getProperty("BFA_BOOTSTRAP_ADMIN_PASSWORD");
            if (u == null || e == null || p == null)
                return;
            Rol admin = roles.findByNombre("ADMINISTRADOR")
                    .orElseThrow(() -> new IllegalStateException("No existe el rol ADMINISTRADOR"));
            Usuario user = new Usuario();
            user.setId(java.util.UUID.randomUUID());
            user.setNombreUsuario(u);
            user.setCorreo(e);
            user.setNombreCompleto(u);
            user.setHashContrasena(encoder.encode(p));
            user.setEstado(EstadoGeneral.ACTIVO);
            user.setCreadoEn(LocalDateTime.now());
            user.setActualizadoEn(LocalDateTime.now());
            users.save(user);
            usuarioRoles.save(new UsuarioRol(user, admin));
        };
    }
}
