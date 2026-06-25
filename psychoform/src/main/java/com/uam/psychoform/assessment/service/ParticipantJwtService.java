package com.uam.psychoform.assessment.service;

import com.uam.psychoform.security.service.JwtService;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ParticipantJwtService {
    private static final String PREFIX = "participant-assignment:";
    private final JwtService jwt;

    public ParticipantJwtService(JwtService jwt) {
        this.jwt = jwt;
    }

    public String issue(UUID participantId, long assignmentId) {
        return jwt.emitir(participantId, PREFIX + assignmentId, Set.of("RESPUESTA_REGISTRAR"), Set.of("PARTICIPANTE"));
    }

    public long requireAssignment(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Token participante requerido");
        }
        var principal = jwt.validar(authorizationHeader.substring(7));
        if (!principal.roles().contains("PARTICIPANTE") || !principal.username().startsWith(PREFIX)) {
            throw new org.springframework.security.access.AccessDeniedException("Token participante invalido");
        }
        return Long.parseLong(principal.username().substring(PREFIX.length()));
    }
}

