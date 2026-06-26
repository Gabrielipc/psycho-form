package com.uam.psychoform.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uam.psychoform.PsychoformApplication;
import com.uam.psychoform.security.service.AuthService;
import com.uam.psychoform.security.service.JwtService;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(classes = PsychoformApplication.class)
@AutoConfigureMockMvc
@Import(HttpSecurityIntegrationTest.SecurityProbeController.class)
class HttpSecurityIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private JwtService jwt;
    @MockitoBean private AuthService auth;

    @Test
    void loginValidoDevuelveBearerEInvalidoDevuelve401() throws Exception {
        AuthService.LoginResult login = new AuthService.LoginResult("access-token", "Bearer", Instant.parse("2026-06-24T08:00:00Z"), UUID.randomUUID(), "ana", Set.of("TEST_CREAR"));
        when(auth.login("ana", "secreta")).thenReturn(login);

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"ana\",\"password\":\"secreta\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
        when(auth.login("ana", "incorrecta")).thenThrow(new IllegalArgumentException("Credenciales inválidas"));
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"ana\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void sinTokenRecibe401SinPermisoRecibe403YPermisoCorrectoAccede() throws Exception {
        mvc.perform(get("/_test/security/probe")).andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(get("/_test/security/probe").header("Authorization", bearer("OTRO_PERMISO")))
                .andExpect(status().isForbidden()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(get("/_test/security/probe").header("Authorization", bearer("TEST_CREAR")))
                .andExpect(status().isOk()).andExpect(content().string("ok"));
    }

    @Test
    void healthYSwaggerNoQuedanPublicos() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(get("/v3/api-docs")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    private String bearer(String permission) {
        return "Bearer " + jwt.emitir(UUID.randomUUID(), "ana", Set.of(permission), Set.of("ROL_PRUEBA"));
    }

    @RestController
    static class SecurityProbeController {
        @GetMapping("/_test/security/probe")
        @PreAuthorize("hasAuthority('PERM_TEST_CREAR')")
        String probe() { return "ok"; }
    }
}
