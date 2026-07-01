package com.uam.psychoform.instrument.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uam.psychoform.PsychoformApplication;
import com.uam.psychoform.instrument.dto.ImageResourceDTO;
import com.uam.psychoform.instrument.dto.ItemDTO;
import com.uam.psychoform.instrument.dto.OptionDTO;
import com.uam.psychoform.instrument.dto.SubtestCloneTemplateDTO;
import com.uam.psychoform.instrument.dto.VersionConfigurationRequest;
import com.uam.psychoform.instrument.model.EstadoVersionTest;
import com.uam.psychoform.instrument.model.Subtest;
import com.uam.psychoform.instrument.model.TipoItem;
import com.uam.psychoform.instrument.model.TipoRespuesta;
import com.uam.psychoform.instrument.model.VersionTest;
import com.uam.psychoform.instrument.service.ImageUploadResponse;
import com.uam.psychoform.instrument.service.InstrumentAdminService;
import com.uam.psychoform.instrument.service.InstrumentImageService;
import com.uam.psychoform.instrument.service.VersionTestService;
import com.uam.psychoform.security.model.EstadoGeneral;
import com.uam.psychoform.security.service.JwtService;
import java.math.BigDecimal;
import java.util.List;
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
class InstrumentReadbackControllerWebTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    JwtService jwt;

    @MockitoBean
    InstrumentAdminService admin;

    @MockitoBean
    VersionTestService versions;

    @MockitoBean
    InstrumentImageService images;

    @Test
    void getItemsReturnsImagesInEachItemDto() throws Exception {
        when(admin.listItemDtos(20L)).thenReturn(List.of(new ItemDTO(10L, "IT-1",
                TipoItem.TEXTO_E_IMAGEN, TipoRespuesta.OPCION_UNICA, "Pregunta", "Instruccion", 1,
                BigDecimal.ONE, null, true, true, EstadoGeneral.ACTIVO,
                List.of(new ImageResourceDTO(55L, 1, "Figura", "test-config/items/10/file.png", null,
                        "ENUNCIADO")))));
        String token = token("TEST_CREAR");

        mvc.perform(get("/subtests/{id}/items", 20L).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].imagenes[0].id").value(55))
                .andExpect(jsonPath("$.data[0].imagenes[0].numeroOrden").value(1))
                .andExpect(jsonPath("$.data[0].imagenes[0].textoAlternativo").value("Figura"))
                .andExpect(jsonPath("$.data[0].imagenes[0].rutaAlmacenamiento")
                        .value("test-config/items/10/file.png"))
                .andExpect(jsonPath("$.data[0].imagenes[0].rol").value("ENUNCIADO"));
    }

    @Test
    void putItemImageByOrderReturnsReplacementResponse() throws Exception {
        when(images.replaceItemImageByOrder(org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.eq(1),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("Nueva"),
                org.mockito.ArgumentMatchers.eq("ENUNCIADO")))
                .thenReturn(new ImageUploadResponse(55L, 77L, "test-config/items/10/new.png", "new.png",
                        "image/png", 3L, "hash", 1, "Nueva", "ENUNCIADO"));
        MockMultipartFile file = new MockMultipartFile("file", "new.png", "image/png", "img".getBytes());

        mvc.perform(multipart("/items/{itemId}/images/by-order/{order}", 10L, 1)
                .file(file)
                .param("altText", "Nueva")
                .param("role", "ENUNCIADO")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .header("Authorization", "Bearer " + token("TEST_CREAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imageLinkId").value(55))
                .andExpect(jsonPath("$.data.storagePath").value("test-config/items/10/new.png"));
    }

    @Test
    void getOptionsReturnsImagesInEachOptionDto() throws Exception {
        when(admin.listOptionDtos(10L)).thenReturn(List.of(new OptionDTO(30L, "A", "Opcion A", 1,
                BigDecimal.ONE, EstadoGeneral.ACTIVO,
                List.of(new ImageResourceDTO(66L, 1, "Opcion", "test-config/options/30/file.png", null,
                        null)))));

        mvc.perform(get("/items/{id}/options", 10L).header("Authorization", "Bearer " + token("TEST_CREAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].imagenes[0].id").value(66))
                .andExpect(jsonPath("$.data[0].imagenes[0].numeroOrden").value(1))
                .andExpect(jsonPath("$.data[0].imagenes[0].textoAlternativo").value("Opcion"))
                .andExpect(jsonPath("$.data[0].imagenes[0].rutaAlmacenamiento")
                        .value("test-config/options/30/file.png"));
    }

    @Test
    void getCloneTemplateReturnsDeepDraftSnapshot() throws Exception {
        when(admin.getCloneTemplate(20L)).thenReturn(new SubtestCloneTemplateDTO(20L, "MEM", "Memoria",
                "Desc", "Inst", 1, 120, true, false, true, null, EstadoGeneral.ACTIVO,
                List.of(new SubtestCloneTemplateDTO.ItemTemplate(30L, "I-1", TipoItem.TEXTO_E_IMAGEN,
                        TipoRespuesta.OPCION_UNICA, "Pregunta", "Seleccione", 1, BigDecimal.ONE, null,
                        true, true, EstadoGeneral.ACTIVO,
                        List.of(new SubtestCloneTemplateDTO.ImageTemplate(60L, 70L, 1, "Figura",
                                "items/30/a.png", "ENUNCIADO")),
                        List.of(new SubtestCloneTemplateDTO.OptionTemplate(40L, "A", "Opcion A", 1,
                                BigDecimal.ONE, EstadoGeneral.ACTIVO, List.of())),
                        new SubtestCloneTemplateDTO.AnswerKeyTemplate(50L, 80L, 40L, null, null, null,
                                BigDecimal.ONE, false)))));

        mvc.perform(get("/subtests/{id}/clone-template", 20L)
                .header("Authorization", "Bearer " + token("TEST_CREAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sourceSubtestId").value(20))
                .andExpect(jsonPath("$.data.items[0].sourceItemId").value(30))
                .andExpect(jsonPath("$.data.items[0].options[0].sourceOptionId").value(40))
                .andExpect(jsonPath("$.data.items[0].images[0].resourceId").value(70))
                .andExpect(jsonPath("$.data.items[0].answerKey.correctOptionSourceId").value(40));
    }

    @Test
    void postVersionConfigurationDelegatesFinalDraftSave() throws Exception {
        VersionTest version = new VersionTest();
        version.setId(99L);
        version.setEstado(EstadoVersionTest.BORRADOR);
        Subtest saved = new Subtest();
        saved.setId(120L);
        saved.setVersionTest(version);
        saved.setCodigoSubtest("MEM-COPY");
        saved.setNombreSubtest("Memoria copia");
        when(admin.saveConfiguration(org.mockito.ArgumentMatchers.eq(99L),
                org.mockito.ArgumentMatchers.any(VersionConfigurationRequest.class)))
                .thenReturn(List.of(saved));

        String body = """
                {
                  "subtests": [
                    {
                      "draftId": "tmp-subtest-1",
                      "sourceId": 20,
                      "status": "CREATED",
                      "code": "MEM-COPY",
                      "name": "Memoria copia",
                      "order": 1,
                      "items": []
                    }
                  ]
                }
                """;

        mvc.perform(post("/test-versions/{id}/configuration", 99L)
                .header("Authorization", "Bearer " + token("TEST_CREAR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(120))
                .andExpect(jsonPath("$.data[0].codigoSubtest").value("MEM-COPY"));
    }

    private String token(String permission) {
        return jwt.emitir(UUID.fromString("44444444-4444-4444-4444-444444444444"), "ana",
                Set.of(permission), Set.of("PSICOLOGO_COORDINADOR"));
    }
}
