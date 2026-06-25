package com.uam.psychoform.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    @Test
    void emiteYValidaUnTokenConPermisos() {
        JwtService service = new JwtService("01234567890123456789012345678901", Clock.fixed(Instant.parse("2026-06-24T00:00:00Z"), ZoneOffset.UTC));
        UUID id = UUID.randomUUID();
        String token = service.emitir(id, "ana", Set.of("TEST_CREAR"), Set.of("PSICOLOGO_COORDINADOR"));
        assertThat(service.validar(token).usuarioId()).isEqualTo(id);
        assertThat(service.validar(token).permisos()).containsExactly("TEST_CREAR");
    }

    @Test
    void rechazaUnTokenManipulado() {
        JwtService service = new JwtService("01234567890123456789012345678901", Clock.systemUTC());
        String token = service.emitir(UUID.randomUUID(), "ana", Set.of(), Set.of());
        assertThatThrownBy(() -> service.validar(token + "x")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaUnTokenVencido() {
        JwtService issuer = new JwtService("01234567890123456789012345678901", Clock.fixed(Instant.parse("2026-06-24T00:00:00Z"), ZoneOffset.UTC));
        JwtService validator = new JwtService("01234567890123456789012345678901", Clock.fixed(Instant.parse("2026-06-24T08:00:01Z"), ZoneOffset.UTC));
        String token = issuer.emitir(UUID.randomUUID(), "ana", Set.of(), Set.of());
        assertThatThrownBy(() -> validator.validar(token)).isInstanceOf(IllegalArgumentException.class);
    }
}
