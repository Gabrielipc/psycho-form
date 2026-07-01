package com.uam.psychoform.instrument.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.uam.psychoform.instrument.model.*;
import com.uam.psychoform.instrument.dto.ImageResourceDTO;
import com.uam.psychoform.instrument.dto.VersionConfigurationRequest;
import com.uam.psychoform.instrument.repository.VersionTestRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.model.EstadoGeneral;
import com.uam.psychoform.security.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class InstrumentAdminServiceConfigurationEditTest {
    private final EntityManager em = Mockito.mock(EntityManager.class);
    private final VersionTestRepository versions = Mockito.mock(VersionTestRepository.class);
    private final UsuarioRepository users = Mockito.mock(UsuarioRepository.class);
    private final CurrentActor currentActor = Mockito.mock(CurrentActor.class);
    private final InstrumentAdminService service = new InstrumentAdminService(em, versions, users, currentActor,
            Clock.fixed(Instant.parse("2026-06-26T10:15:30Z"), ZoneOffset.UTC));

    @BeforeEach
    void resetActor() {
        when(currentActor.usuarioId()).thenReturn(java.util.UUID.fromString("44444444-4444-4444-4444-444444444444"));
    }

    @Test
    void listOptionsReturnsOptionsOrderedByItem() {
        OpcionItem a = new OpcionItem();
        a.setCodigoOpcion("A");
        OpcionItem b = new OpcionItem();
        b.setCodigoOpcion("B");
        TypedQuery<OpcionItem> query = Mockito.mock();
        when(em.createQuery("SELECT o FROM OpcionItem o WHERE o.item.id = :itemId ORDER BY o.numeroOrden ASC", OpcionItem.class))
                .thenReturn(query);
        when(query.setParameter("itemId", 10L)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(a, b));

        assertThat(service.listOptions(10L)).extracting(OpcionItem::getCodigoOpcion).containsExactly("A", "B");
    }

    @Test
    void listItemDtosIncludesOrderedImages() {
        Item item = new Item();
        item.setId(10L);
        item.setCodigoItem("IT-1");
        item.setTipoItem(TipoItem.TEXTO_E_IMAGEN);
        item.setTipoRespuesta(TipoRespuesta.OPCION_UNICA);
        item.setNumeroOrden(1);
        item.setPuntajeBase(BigDecimal.ONE);
        item.setEsObligatorio(true);
        item.setEsConfidencial(true);
        item.setEstado(com.uam.psychoform.security.model.EstadoGeneral.ACTIVO);
        TypedQuery<Item> query = Mockito.mock();
        when(em.createQuery("SELECT i FROM Item i WHERE i.subtest.id = :subtestId ORDER BY i.numeroOrden ASC", Item.class))
                .thenReturn(query);
        when(query.setParameter("subtestId", 20L)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(item));
        TypedQuery<Object[]> imageQuery = Mockito.mock();
        when(em.createQuery("""
                SELECT img.item.id, img.id, img.numeroOrden, img.textoAlternativo, res.rutaAlmacenamiento,
                       img.rolImagen
                FROM ImagenItem img
                JOIN img.recurso res
                WHERE img.item.id IN :itemIds
                ORDER BY img.item.id ASC, img.numeroOrden ASC
                """, Object[].class)).thenReturn(imageQuery);
        when(imageQuery.setParameter("itemIds", List.of(10L))).thenReturn(imageQuery);
        when(imageQuery.getResultList()).thenReturn(List.<Object[]>of(
                new Object[] { 10L, 55L, 1, "Figura", "test-config/items/10/file.png", "ENUNCIADO" }));

        assertThat(service.listItemDtos(20L).getFirst().imagenes())
                .extracting(ImageResourceDTO::rutaAlmacenamiento)
                .containsExactly("test-config/items/10/file.png");
    }

    @Test
    void updateItemRequiresDraftVersion() {
        Item item = itemWithVersion(10L, 20L, EstadoVersionTest.PUBLICADO);
        when(em.find(Item.class, 10L)).thenReturn(item);
        when(versions.findByIdForUpdate(20L)).thenReturn(Optional.of(item.getSubtest().getVersionTest()));

        assertThatThrownBy(() -> service.updateItem(10L,
                new InstrumentAdminService.ItemCommand("IT-2", TipoItem.SOLO_TEXTO, TipoRespuesta.TEXTO_ABIERTO,
                        "Pregunta", "Instruccion", 2, BigDecimal.TEN, 90, true, false)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no se edita directamente");
    }

    @Test
    void updateOptionMutatesDraftOptionFields() {
        Item item = itemWithVersion(10L, 20L, EstadoVersionTest.BORRADOR);
        OpcionItem option = new OpcionItem();
        option.setId(30L);
        option.setItem(item);
        option.setCodigoOpcion("A");
        when(em.find(OpcionItem.class, 30L)).thenReturn(option);
        when(versions.findByIdForUpdate(20L)).thenReturn(Optional.of(item.getSubtest().getVersionTest()));

        OpcionItem updated = service.updateOption(30L,
                new InstrumentAdminService.OptionCommand("B", "Texto", 2, BigDecimal.valueOf(4)));

        assertThat(updated.getCodigoOpcion()).isEqualTo("B");
        assertThat(updated.getTextoOpcion()).isEqualTo("Texto");
        assertThat(updated.getNumeroOrden()).isEqualTo(2);
        assertThat(updated.getValorOrdinal()).isEqualByComparingTo("4");
    }

    @Test
    void findAnswerKeyReturnsNullWhenItemHasNoKey() {
        TypedQuery<ClaveRespuesta> query = Mockito.mock();
        when(em.createQuery("SELECT c FROM ClaveRespuesta c WHERE c.item.id = :itemId", ClaveRespuesta.class))
                .thenReturn(query);
        when(query.setParameter("itemId", 10L)).thenReturn(query);
        when(query.setMaxResults(1)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        assertThat(service.findAnswerKey(10L)).isNull();
    }

    @Test
    void cloneTemplateReturnsSubtestItemsOptionsImagesAndAnswerKeysWithoutPersisting() {
        Subtest source = subtestWithVersion(20L, 5L, EstadoVersionTest.PUBLICADO);
        source.setCodigoSubtest("MEM");
        source.setNombreSubtest("Memoria");
        source.setDescripcion("Desc");
        source.setInstrucciones("Instrucciones");
        source.setNumeroOrden(1);
        source.setTiempoLimiteSegundos(120);
        source.setPermiteAleatorizarItems(true);
        source.setPermiteAleatorizarOpciones(false);
        source.setEsObligatorio(true);
        source.setEstado(EstadoGeneral.ACTIVO);
        when(em.find(Subtest.class, 20L)).thenReturn(source);

        Item item = new Item();
        item.setId(30L);
        item.setSubtest(source);
        item.setCodigoItem("I-1");
        item.setTipoItem(TipoItem.TEXTO_E_IMAGEN);
        item.setTipoRespuesta(TipoRespuesta.OPCION_UNICA);
        item.setEnunciado("Pregunta");
        item.setInstruccion("Seleccione");
        item.setNumeroOrden(1);
        item.setPuntajeBase(BigDecimal.ONE);
        item.setEsObligatorio(true);
        item.setEsConfidencial(true);
        item.setEstado(EstadoGeneral.ACTIVO);
        TypedQuery<Item> itemQuery = Mockito.mock();
        when(em.createQuery("SELECT i FROM Item i WHERE i.subtest.id = :subtestId ORDER BY i.numeroOrden ASC", Item.class))
                .thenReturn(itemQuery);
        when(itemQuery.setParameter("subtestId", 20L)).thenReturn(itemQuery);
        when(itemQuery.getResultList()).thenReturn(List.of(item));

        OpcionItem option = new OpcionItem();
        option.setId(40L);
        option.setItem(item);
        option.setCodigoOpcion("A");
        option.setTextoOpcion("Opcion A");
        option.setNumeroOrden(1);
        option.setValorOrdinal(BigDecimal.TEN);
        option.setEstado(EstadoGeneral.ACTIVO);
        TypedQuery<OpcionItem> optionQuery = Mockito.mock();
        when(em.createQuery("""
                SELECT o FROM OpcionItem o
                WHERE o.item.id IN :itemIds
                ORDER BY o.item.id ASC, o.numeroOrden ASC
                """, OpcionItem.class)).thenReturn(optionQuery);
        when(optionQuery.setParameter("itemIds", List.of(30L))).thenReturn(optionQuery);
        when(optionQuery.getResultList()).thenReturn(List.of(option));

        ClaveRespuesta key = new ClaveRespuesta();
        key.setId(50L);
        key.setItem(item);
        key.setOpcionCorrecta(option);
        key.setTextoEsperado("A");
        key.setPuntaje(BigDecimal.ONE);
        key.setRequiereRevisionManual(false);
        TypedQuery<ClaveRespuesta> keyQuery = Mockito.mock();
        when(em.createQuery("""
                SELECT c FROM ClaveRespuesta c
                LEFT JOIN FETCH c.opcionCorrecta
                WHERE c.item.id IN :itemIds
                """, ClaveRespuesta.class)).thenReturn(keyQuery);
        when(keyQuery.setParameter("itemIds", List.of(30L))).thenReturn(keyQuery);
        when(keyQuery.getResultList()).thenReturn(List.of(key));

        TypedQuery<Object[]> itemImageQuery = Mockito.mock();
        when(em.createQuery("""
                SELECT img.item.id, img.id, img.recurso.id, img.numeroOrden, img.textoAlternativo,
                       res.rutaAlmacenamiento, img.rolImagen
                FROM ImagenItem img
                JOIN img.recurso res
                WHERE img.item.id IN :itemIds
                ORDER BY img.item.id ASC, img.numeroOrden ASC
                """, Object[].class)).thenReturn(itemImageQuery);
        when(itemImageQuery.setParameter("itemIds", List.of(30L))).thenReturn(itemImageQuery);
        when(itemImageQuery.getResultList()).thenReturn(List.<Object[]>of(
                new Object[] { 30L, 60L, 70L, 1, "Figura", "items/30/a.png", "ENUNCIADO" }));

        TypedQuery<Object[]> optionImageQuery = Mockito.mock();
        when(em.createQuery("""
                SELECT img.opcion.id, img.id, img.recurso.id, img.numeroOrden, img.textoAlternativo,
                       res.rutaAlmacenamiento
                FROM ImagenOpcion img
                JOIN img.recurso res
                WHERE img.opcion.id IN :optionIds
                ORDER BY img.opcion.id ASC, img.numeroOrden ASC
                """, Object[].class)).thenReturn(optionImageQuery);
        when(optionImageQuery.setParameter("optionIds", List.of(40L))).thenReturn(optionImageQuery);
        when(optionImageQuery.getResultList()).thenReturn(List.<Object[]>of(
                new Object[] { 40L, 61L, 71L, 1, "Opcion", "options/40/a.png" }));

        var template = service.getCloneTemplate(20L);

        assertThat(template.sourceSubtestId()).isEqualTo(20L);
        assertThat(template.items()).hasSize(1);
        assertThat(template.items().getFirst().sourceItemId()).isEqualTo(30L);
        assertThat(template.items().getFirst().options().getFirst().sourceOptionId()).isEqualTo(40L);
        assertThat(template.items().getFirst().images().getFirst().resourceId()).isEqualTo(70L);
        assertThat(template.items().getFirst().options().getFirst().images().getFirst().resourceId()).isEqualTo(71L);
        assertThat(template.items().getFirst().answerKey().correctOptionSourceId()).isEqualTo(40L);
        Mockito.verify(em, Mockito.never()).persist(Mockito.any());
    }

    @Test
    void saveConfigurationPersistsCreatedDraftGraphOnlyWhenVersionIsDraft() {
        VersionTest version = new VersionTest();
        version.setId(99L);
        version.setEstado(EstadoVersionTest.BORRADOR);
        when(versions.findByIdForUpdate(99L)).thenReturn(Optional.of(version));
        RecursoMultimedia resource = new RecursoMultimedia();
        resource.setId(70L);
        when(em.find(RecursoMultimedia.class, 70L)).thenReturn(resource);

        VersionConfigurationRequest.ImageDraft image = new VersionConfigurationRequest.ImageDraft(null,
                "tmp-image-1", 60L, 70L, VersionConfigurationRequest.DraftStatus.CREATED, 1, "Figura",
                "ENUNCIADO");
        VersionConfigurationRequest.OptionDraft option = new VersionConfigurationRequest.OptionDraft(null,
                "tmp-option-1", 40L, VersionConfigurationRequest.DraftStatus.CREATED, "A", "Opcion A", 1,
                BigDecimal.ONE, List.of());
        VersionConfigurationRequest.ItemDraft item = new VersionConfigurationRequest.ItemDraft(null, "tmp-item-1",
                30L, VersionConfigurationRequest.DraftStatus.CREATED, "I-1", TipoItem.TEXTO_E_IMAGEN,
                TipoRespuesta.OPCION_UNICA, "Pregunta", "Seleccione", 1, BigDecimal.ONE, null, true, true,
                List.of(image), List.of(option), null);
        VersionConfigurationRequest.SubtestDraft subtest = new VersionConfigurationRequest.SubtestDraft(null,
                "tmp-subtest-1", 20L, VersionConfigurationRequest.DraftStatus.CREATED, "MEM-COPY", "Memoria copia",
                "Desc", "Inst", 1, 120, true, false, true, null, List.of(item));
        VersionConfigurationRequest request = new VersionConfigurationRequest(List.of(subtest));

        service.saveConfiguration(99L, request);

        ArgumentCaptor<Object> persisted = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(em, Mockito.atLeast(4)).persist(persisted.capture());
        assertThat(persisted.getAllValues()).anyMatch(Subtest.class::isInstance);
        assertThat(persisted.getAllValues()).anyMatch(Item.class::isInstance);
        assertThat(persisted.getAllValues()).anyMatch(OpcionItem.class::isInstance);
        assertThat(persisted.getAllValues()).anyMatch(ImagenItem.class::isInstance);
    }

    @Test
    void saveConfigurationRejectsPublishedVersion() {
        VersionTest version = new VersionTest();
        version.setId(99L);
        version.setEstado(EstadoVersionTest.PUBLICADO);
        when(versions.findByIdForUpdate(99L)).thenReturn(Optional.of(version));

        assertThatThrownBy(() -> service.saveConfiguration(99L, new VersionConfigurationRequest(List.of())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no se edita directamente");
    }

    @Test
    void saveConfigurationRejectsDuplicateSubtestCodeBeforePersisting() {
        VersionTest version = new VersionTest();
        version.setId(99L);
        version.setEstado(EstadoVersionTest.BORRADOR);
        when(versions.findByIdForUpdate(99L)).thenReturn(Optional.of(version));
        VersionConfigurationRequest.SubtestDraft first = subtestDraft("tmp-1", "MEM", 1);
        VersionConfigurationRequest.SubtestDraft second = subtestDraft("tmp-2", "MEM", 2);

        assertThatThrownBy(() -> service.saveConfiguration(99L,
                new VersionConfigurationRequest(List.of(first, second))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Codigo de subtest duplicado");
        Mockito.verify(em, Mockito.never()).persist(Mockito.any());
    }

    @Test
    void saveConfigurationRejectsDuplicateItemOrderBeforePersisting() {
        VersionTest version = new VersionTest();
        version.setId(99L);
        version.setEstado(EstadoVersionTest.BORRADOR);
        when(versions.findByIdForUpdate(99L)).thenReturn(Optional.of(version));
        VersionConfigurationRequest.ItemDraft first = itemDraft("tmp-item-1", "I-1", 1);
        VersionConfigurationRequest.ItemDraft second = itemDraft("tmp-item-2", "I-2", 1);
        VersionConfigurationRequest.SubtestDraft subtest = new VersionConfigurationRequest.SubtestDraft(null,
                "tmp-subtest-1", null, VersionConfigurationRequest.DraftStatus.CREATED, "MEM", "Memoria",
                null, null, 1, null, false, false, true, null, List.of(first, second));

        assertThatThrownBy(() -> service.saveConfiguration(99L,
                new VersionConfigurationRequest(List.of(subtest))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Orden de item duplicado");
        Mockito.verify(em, Mockito.never()).persist(Mockito.any());
    }

    private static Item itemWithVersion(long itemId, long versionId, EstadoVersionTest state) {
        Subtest subtest = subtestWithVersion(1L, versionId, state);
        Item item = new Item();
        item.setId(itemId);
        item.setSubtest(subtest);
        return item;
    }

    private static Subtest subtestWithVersion(long subtestId, long versionId, EstadoVersionTest state) {
        VersionTest version = new VersionTest();
        version.setId(versionId);
        version.setEstado(state);
        Subtest subtest = new Subtest();
        subtest.setId(subtestId);
        subtest.setVersionTest(version);
        return subtest;
    }

    private static VersionConfigurationRequest.SubtestDraft subtestDraft(String draftId, String code, int order) {
        return new VersionConfigurationRequest.SubtestDraft(null, draftId, null,
                VersionConfigurationRequest.DraftStatus.CREATED, code, "Subtest " + code, null, null, order,
                null, false, false, true, null, List.of());
    }

    private static VersionConfigurationRequest.ItemDraft itemDraft(String draftId, String code, int order) {
        return new VersionConfigurationRequest.ItemDraft(null, draftId, null,
                VersionConfigurationRequest.DraftStatus.CREATED, code, TipoItem.SOLO_TEXTO,
                TipoRespuesta.TEXTO_ABIERTO, "Pregunta", null, order, BigDecimal.ONE, null, true, true,
                List.of(), List.of(), null);
    }
}

