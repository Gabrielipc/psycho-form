package com.uam.psychoform.security.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uam.psychoform.security.JwtProperties;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final byte[] secret;
    private final Clock clock;

    @Autowired
    public JwtService(JwtProperties jwtProperties, Clock clock) {
        this(requireSecret(jwtProperties.getSecret()), clock);
    }

    JwtService(String secret, Clock clock) {
        if (secret == null || secret.length() < 32)
            throw new IllegalArgumentException("BFA_JWT_SECRET debe tener al menos 32 caracteres");
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.clock = clock;
    }

    public String emitir(UUID id, String username, Set<String> permisos, Set<String> roles) {
        try {
            long now = Instant.now(clock).getEpochSecond();
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("sub", id.toString());
            claims.put("username", username);
            claims.put("permissions", permisos);
            claims.put("roles", roles);
            claims.put("iat", now);
            claims.put("exp", now + 28_800);
            String header = base64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
            String payload = base64(JSON.writeValueAsString(claims));
            String signed = header + "." + payload;
            return signed + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(hmac(signed));
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible emitir JWT", e);
        }
    }

    public JwtPrincipal validar(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3
                    || !MessageDigest.isEqual(hmac(parts[0] + "." + parts[1]), Base64.getUrlDecoder().decode(parts[2])))
                throw new IllegalArgumentException("JWT inválido");
            Map<String, Object> claims = JSON.readValue(Base64.getUrlDecoder().decode(parts[1]), new TypeReference<>() {
            });
            if (((Number) claims.get("exp")).longValue() <= Instant.now(clock).getEpochSecond())
                throw new IllegalArgumentException("JWT expirado");
            Set<String> permissions = stringSet(claims.get("permissions"));
            Set<String> roles = stringSet(claims.get("roles"));
            return new JwtPrincipal(UUID.fromString((String) claims.get("sub")), (String) claims.get("username"),
                    permissions, roles, ((Number) claims.get("exp")).longValue());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("JWT inválido", e);
        }
    }

    private byte[] hmac(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String base64(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static Set<String> stringSet(Object claim) {
        if (!(claim instanceof java.util.Collection<?> values))
            throw new IllegalArgumentException("Claim de colección inválido");
        return values.stream().filter(String.class::isInstance).map(String.class::cast)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static String requireSecret(String secret) {
        return secret;
    }

    public record JwtPrincipal(UUID usuarioId, String username, Set<String> permisos, Set<String> roles,
            long expiresAtEpochSecond) {
    }
}
