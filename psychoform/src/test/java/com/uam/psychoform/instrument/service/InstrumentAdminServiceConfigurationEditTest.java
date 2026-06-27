package com.uam.psychoform.instrument.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.uam.psychoform.instrument.model.*;
import com.uam.psychoform.instrument.dto.ImageResourceDTO;
import com.uam.psychoform.instrument.repository.VersionTestRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class InstrumentAdminServiceConfigurationEditTest {
    private final EntityManager em = Mockito.mock(EntityManager.class);
    private final VersionTestRepository versions = Mockito.mock(VersionTestRepository.class);
    private final UsuarioRepository users = Mockito.mock(UsuarioRepository.class);
    private final CurrentActor currentActor = Mockito.mock(CurrentActor.class);
    private final InstrumentAdminService service = new InstrumentAdminService(em, versions, users, currentActor,
            Clock.fixed(Instant.parse("2026-06-26T10:15:30Z"), ZoneOffset.UTC));

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

    private static Item itemWithVersion(long itemId, long versionId, EstadoVersionTest state) {
        VersionTest version = new VersionTest();
        version.setId(versionId);
        version.setEstado(state);
        Subtest subtest = new Subtest();
        subtest.setVersionTest(version);
        Item item = new Item();
        item.setId(itemId);
        item.setSubtest(subtest);
        return item;
    }
}

