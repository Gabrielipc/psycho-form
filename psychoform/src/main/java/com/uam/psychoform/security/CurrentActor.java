package com.uam.psychoform.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentActor {
    public java.util.UUID usuarioId() {
        return principal().usuarioId();
    }

    public String username() {
        return principal().username();
    }

    public com.uam.psychoform.security.service.JwtService.JwtPrincipal principal() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof com.uam.psychoform.security.service.JwtService.JwtPrincipal actor)
            return actor;
        throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                "No hay actor autenticado");
    }
}
