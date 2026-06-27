package com.uam.psychoform.instrument.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uam.psychoform.PsychoformApplication;
import com.uam.psychoform.instrument.service.ImageUploadResponse;
import com.uam.psychoform.instrument.service.InstrumentImageService;
import com.uam.psychoform.security.service.JwtService;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = PsychoformApplication.class)
@AutoConfigureMockMvc
class InstrumentImageControllerWebTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    JwtService jwt;

    @MockitoBean
    InstrumentImageService images;

    @Test
    void itemImageUploadReturnsStandardEnvelope() throws Exception {
        when(images.uploadItemImage(eq(10L), any(), eq(2), eq("Figura"), eq("ENUNCIADO")))
                .thenReturn(new ImageUploadResponse(55L, 77L, "test-config/items/10/file.png", "file.png",
                        "image/png", 3L, "hash", 2, "Figura", "ENUNCIADO"));
        String token = jwt.emitir(UUID.fromString("22222222-2222-2222-2222-222222222222"), "ana",
                Set.of("TEST_CREAR"), Set.of("PSICOLOGO_COORDINADOR"));
        MockMultipartFile file = new MockMultipartFile("file", "file.png", "image/png", "img".getBytes());

        mvc.perform(multipart("/items/{id}/images", 10L)
                .file(file)
                .param("order", "2")
                .param("altText", "Figura")
                .param("role", "ENUNCIADO")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.imageLinkId").value(55))
                .andExpect(jsonPath("$.data.resourceId").value(77))
                .andExpect(jsonPath("$.data.storagePath").value("test-config/items/10/file.png"))
                .andExpect(jsonPath("$.data.role").value("ENUNCIADO"));
    }

    @Test
    void itemImageUploadRequiresTestCreatePermission() throws Exception {
        String token = jwt.emitir(UUID.fromString("33333333-3333-3333-3333-333333333333"), "ana",
                Set.of("TEST_PUBLICAR"), Set.of("PSICOLOGO_COORDINADOR"));
        MockMultipartFile file = new MockMultipartFile("file", "file.png", "image/png", "img".getBytes());

        mvc.perform(multipart("/items/{id}/images", 10L)
                .file(file)
                .param("order", "2")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

        verify(images, never()).uploadItemImage(anyLong(), any(), any(), any(), any());
    }
}
