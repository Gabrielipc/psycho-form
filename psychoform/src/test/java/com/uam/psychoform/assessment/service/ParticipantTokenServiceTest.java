package com.uam.psychoform.assessment.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class ParticipantTokenServiceTest {

    @Test
    void generaTokenCrudoYLoValidaContraHashSinPersistirElValorCrudo() {
        ParticipantTokenService service = new ParticipantTokenService(new BCryptPasswordEncoder());

        String raw = service.generateRawToken();
        String hash = service.hash(raw);

        assertThat(raw).hasSizeGreaterThanOrEqualTo(32);
        assertThat(hash).isNotEqualTo(raw);
        assertThat(service.matches(raw, hash)).isTrue();
        assertThat(service.matches("wrong-token", hash)).isFalse();
    }
}
