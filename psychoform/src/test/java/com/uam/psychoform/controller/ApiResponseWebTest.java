package com.uam.psychoform.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uam.psychoform.PsychoformApplication;
import com.uam.psychoform.academic.model.Carrera;
import com.uam.psychoform.academic.repository.CarreraRepository;
import com.uam.psychoform.security.model.EstadoGeneral;
import com.uam.psychoform.security.service.AuthService;
import com.uam.psychoform.security.service.JwtService;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = PsychoformApplication.class)
@AutoConfigureMockMvc
class ApiResponseWebTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    JwtService jwt;

    @MockitoBean
    AuthService auth;

    @MockitoBean
    CarreraRepository carreraRepository;

    @Test
    void loginReturnsStandardEnvelope() throws Exception {
        var result = new AuthService.LoginResult("token", "Bearer", Instant.parse("2026-06-25T12:00:00Z"),
                UUID.fromString("11111111-1111-1111-1111-111111111111"), "ana", Set.of("TEST_CREAR"));
        when(auth.login("ana", "secreta")).thenReturn(result);

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ana\",\"password\":\"secreta\"}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.accessToken").value("token"))
                .andExpect(jsonPath("$.message").value("Operacion completada"));
    }

    @Test
    void meReturnsCurrentJwtPrincipal() throws Exception {
        String token = jwt.emitir(UUID.fromString("22222222-2222-2222-2222-222222222222"), "ana",
                Set.of("TEST_CREAR"), Set.of("APLICADOR"));

        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.username").value("ana"))
                .andExpect(jsonPath("$.data.permissions[0]").value("TEST_CREAR"));
    }

    @Test
    void catalogosCarrerasUsesControllerAuthorizationOnly() throws Exception {
        Carrera carrera = new Carrera();
        carrera.setId((short) 1);
        carrera.setCodigoCarrera("SIS");
        carrera.setNombreCarrera("Ingenieria en Sistemas");
        carrera.setEstado(EstadoGeneral.ACTIVO);
        when(carreraRepository.buscarActivos("")).thenReturn(List.of(carrera));

        String token = jwt.emitir(UUID.fromString("33333333-3333-3333-3333-333333333333"), "ana",
                Set.of("CARRERA_LEER"), Set.of("ADMINISTRADOR"));

        mvc.perform(get("/catalogos/carreras").header("Authorization", "Bearer " + token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].codigoCarrera").value("SIS"));
    }

    @Test
    void validationErrorsUseStandardEnvelope() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void unexpectedIllegalArgumentFromLoginReturnsUnauthorizedEnvelope() throws Exception {
        when(auth.login(any(), any())).thenThrow(new IllegalArgumentException("Credenciales invalidas"));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ana\",\"password\":\"bad\"}")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false)).andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }
}
