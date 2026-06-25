package com.uam.psychoform.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.uam.psychoform.security.service.JwtService;
import jakarta.servlet.FilterChain;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {
    @AfterEach void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void tokenValidoAutenticaConPermisosEfectivos() throws Exception {
        JwtService jwt = jwt(Clock.systemUTC());
        MockHttpServletRequest request = request(jwt.emitir(UUID.randomUUID(), "ana", Set.of("TEST_CREAR"), Set.of()));
        FilterChain chain = Mockito.mock(FilterChain.class);

        new JwtAuthenticationFilter(jwt, new SecurityErrorHandler()).doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(Object::toString).containsExactly("PERM_TEST_CREAR");
        verify(chain).doFilter(Mockito.any(), Mockito.any());
    }

    @Test
    void tokenManipuladoYVencidoRecibenRespuestaNoAutorizadaUniforme() throws Exception {
        JwtService jwt = jwt(Clock.fixed(Instant.parse("2026-06-24T12:00:00Z"), ZoneOffset.UTC));
        String vencido = jwt.emitir(UUID.randomUUID(), "ana", Set.of(), Set.of());
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt, new SecurityErrorHandler());

        MockHttpServletResponse manipulated = new MockHttpServletResponse();
        filter.doFilter(request(vencido + "x"), manipulated, Mockito.mock(FilterChain.class));
        assertThat(manipulated.getStatus()).isEqualTo(401);

        MockHttpServletResponse expired = new MockHttpServletResponse();
        JwtService later = jwt(Clock.fixed(Instant.parse("2026-06-24T20:00:01Z"), ZoneOffset.UTC));
        FilterChain chain = Mockito.mock(FilterChain.class);
        new JwtAuthenticationFilter(later, new SecurityErrorHandler()).doFilter(request(vencido), expired, chain);
        assertThat(expired.getStatus()).isEqualTo(401);
        verifyNoInteractions(chain);
    }

    private static MockHttpServletRequest request(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private static JwtService jwt(Clock clock) {
        return new JwtService(new MockEnvironment().withProperty("BFA_JWT_SECRET", "01234567890123456789012345678901"), clock);
    }
}
