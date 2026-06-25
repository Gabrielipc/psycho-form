package com.uam.psychoform.security.service;

import com.uam.psychoform.security.model.EstadoGeneral;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import java.time.Instant;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final SecurityRelationshipService relaciones;
    private final PasswordEncoder passwords;
    private final JwtService jwt;
    private final java.time.Clock clock;

    public AuthService(UsuarioRepository usuarios, SecurityRelationshipService relaciones, PasswordEncoder passwords,
            JwtService jwt, java.time.Clock clock) {
        this.usuarios = usuarios;
        this.relaciones = relaciones;
        this.passwords = passwords;
        this.jwt = jwt;
        this.clock = clock;
    }

    public LoginResult login(String username, String password) {
        Usuario u = usuarios.findByNombreUsuarioAndEstado(username, EstadoGeneral.ACTIVO)
                .orElseThrow(AuthService::denied);
        if (!passwords.matches(password, u.getHashContrasena()))
            throw denied();
        Set<String> permissions = relaciones.permisosEfectivos(u.getId());
        Set<String> roles = relaciones.rolesDeUsuario(u.getId());
        String token = jwt.emitir(u.getId(), u.getNombreUsuario(), permissions, roles);
        return new LoginResult(token, "Bearer", Instant.now(clock).plusSeconds(28800), u.getId(), u.getNombreUsuario(),
                permissions);
    }

    private static IllegalArgumentException denied() {
        return new IllegalArgumentException("Credenciales inválidas");
    }

    public record LoginResult(String accessToken, String tokenType, Instant expiresAt, java.util.UUID userId,
            String username, Set<String> permissions) {
    }
}
