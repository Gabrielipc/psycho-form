package com.uam.psychoform.instrument.service;

import com.uam.psychoform.instrument.model.*;
import com.uam.psychoform.instrument.dto.ImageResourceDTO;
import com.uam.psychoform.instrument.dto.ItemDTO;
import com.uam.psychoform.instrument.dto.OptionDTO;
import com.uam.psychoform.instrument.dto.SubtestCloneTemplateDTO;
import com.uam.psychoform.instrument.dto.VersionConfigurationRequest;
import com.uam.psychoform.instrument.repository.VersionTestRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.SecurityPermissions;
import com.uam.psychoform.security.model.EstadoGeneral;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InstrumentAdminService {
    private final EntityManager em;
    private final VersionTestRepository versions;
    private final UsuarioRepository users;
    private final CurrentActor currentActor;
    private final Clock clock;

    public InstrumentAdminService(EntityManager em, VersionTestRepository versions, UsuarioRepository users,
            CurrentActor currentActor, Clock clock) {
        this.em = em;
        this.versions = versions;
        this.users = users;
        this.currentActor = currentActor;
        this.clock = clock;
    }

    @PreAuthorize(SecurityPermissions.TEST_LEER)
    public List<TestPsicologico> listTests() {
        return em.createQuery("select t from TestPsicologico t", TestPsicologico.class).getResultList();
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public TestPsicologico createTest(TestCommand command) {
        TestPsicologico test = new TestPsicologico();
        test.setCodigoTest(command.code());
        test.setNombreTest(command.name());
        test.setDescripcion(command.description());
        test.setEstado(EstadoGeneral.ACTIVO);
        test.setCreadoPor(currentUser());
        test.setCreadoEn(LocalDateTime.now(clock));
        em.persist(test);
        return test;
    }

    @PreAuthorize(SecurityPermissions.TEST_LEER)
    public List<VersionTest> listVersions(long testId) {
        return em.createQuery("select v from VersionTest v where v.test.id = :testId", VersionTest.class)
                .setParameter("testId", testId).getResultList();
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public VersionTest createVersion(long testId, VersionCommand command) {
        TestPsicologico test = find(TestPsicologico.class, testId);
        VersionTest version = new VersionTest();
        version.setTest(test);
        version.setEstrategiaCalificacion(command.strategyId() == null ? null : find(EstrategiaCalificacion.class, command.strategyId()));
        version.setNumeroVersion(command.number());
        version.setEstado(EstadoVersionTest.BORRADOR);
        version.setInstruccionesGenerales(command.instructions());
        version.setTiempoLimiteSegundos(command.timeLimitSeconds());
        version.setPermiteAleatorizarItems(Boolean.TRUE.equals(command.randomizeItems()));
        version.setPermiteAleatorizarSubtests(Boolean.TRUE.equals(command.randomizeSubtests()));
        version.setCreadoPor(currentUser());
        version.setCreadoEn(LocalDateTime.now(clock));
        em.persist(version);
        return version;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public VersionTest updateVersion(long versionId, VersionCommand command) {
        VersionTest version = requireDraft(versionId);
        version.setEstrategiaCalificacion(command.strategyId() == null ? version.getEstrategiaCalificacion()
                : find(EstrategiaCalificacion.class, command.strategyId()));
        version.setNumeroVersion(command.number());
        version.setInstruccionesGenerales(command.instructions());
        version.setTiempoLimiteSegundos(command.timeLimitSeconds());
        version.setPermiteAleatorizarItems(Boolean.TRUE.equals(command.randomizeItems()));
        version.setPermiteAleatorizarSubtests(Boolean.TRUE.equals(command.randomizeSubtests()));
        return version;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public Subtest createSubtest(long versionId, SubtestCommand command) {
        VersionTest version = requireDraft(versionId);
        Subtest subtest = new Subtest();
        subtest.setVersionTest(version);
        subtest.setEstrategiaCalificacion(command.strategyId() == null ? null : find(EstrategiaCalificacion.class, command.strategyId()));
        subtest.setCodigoSubtest(command.code());
        subtest.setNombreSubtest(command.name());
        subtest.setDescripcion(command.description());
        subtest.setInstrucciones(command.instructions());
        subtest.setNumeroOrden(command.order());
        subtest.setTiempoLimiteSegundos(command.timeLimitSeconds());
        
        boolean randomizeItems = Boolean.TRUE.equals(command.randomizeItems());
        if (randomizeItems && !Boolean.TRUE.equals(version.getPermiteAleatorizarItems())) {
            randomizeItems = false;
        }
        subtest.setPermiteAleatorizarItems(randomizeItems);
        subtest.setPermiteAleatorizarOpciones(Boolean.TRUE.equals(command.randomizeOptions()));
        subtest.setEsObligatorio(!Boolean.FALSE.equals(command.required()));
        subtest.setEstado(EstadoGeneral.ACTIVO);
        em.persist(subtest);
        return subtest;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public Subtest updateSubtest(long subtestId, SubtestCommand command) {
        Subtest subtest = find(Subtest.class, subtestId);
        requireDraft(subtest.getVersionTest().getId());
        subtest.setEstrategiaCalificacion(command.strategyId() == null ? null : find(EstrategiaCalificacion.class, command.strategyId()));
        subtest.setCodigoSubtest(command.code());
        subtest.setNombreSubtest(command.name());
        subtest.setDescripcion(command.description());
        subtest.setInstrucciones(command.instructions());
        subtest.setNumeroOrden(command.order());
        subtest.setTiempoLimiteSegundos(command.timeLimitSeconds());
        
        boolean randomizeItems = Boolean.TRUE.equals(command.randomizeItems());
        if (randomizeItems && !Boolean.TRUE.equals(subtest.getVersionTest().getPermiteAleatorizarItems())) {
            randomizeItems = false;
        }
        subtest.setPermiteAleatorizarItems(randomizeItems);
        subtest.setPermiteAleatorizarOpciones(Boolean.TRUE.equals(command.randomizeOptions()));
        subtest.setEsObligatorio(!Boolean.FALSE.equals(command.required()));
        return subtest;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public Item createItem(long subtestId, ItemCommand command) {
        Subtest subtest = find(Subtest.class, subtestId);
        requireDraft(subtest.getVersionTest().getId());
        Item item = new Item();
        item.setSubtest(subtest);
        item.setCodigoItem(command.code());
        item.setTipoItem(command.itemType());
        item.setTipoRespuesta(command.responseType());
        item.setEnunciado(command.prompt());
        item.setInstruccion(command.instruction());
        item.setNumeroOrden(command.order());
        item.setPuntajeBase(command.baseScore() == null ? BigDecimal.ONE : command.baseScore());
        item.setTiempoLimiteSegundos(command.timeLimitSeconds());
        item.setEsObligatorio(!Boolean.FALSE.equals(command.required()));
        item.setEsConfidencial(!Boolean.FALSE.equals(command.confidential()));
        item.setEstado(EstadoGeneral.ACTIVO);
        em.persist(item);
        return item;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public Item updateItem(long itemId, ItemCommand command) {
        Item item = find(Item.class, itemId);
        requireDraft(item.getSubtest().getVersionTest().getId());
        item.setCodigoItem(command.code());
        item.setTipoItem(command.itemType());
        item.setTipoRespuesta(command.responseType());
        item.setEnunciado(command.prompt());
        item.setInstruccion(command.instruction());
        item.setNumeroOrden(command.order());
        item.setPuntajeBase(command.baseScore() == null ? BigDecimal.ONE : command.baseScore());
        item.setTiempoLimiteSegundos(command.timeLimitSeconds());
        item.setEsObligatorio(!Boolean.FALSE.equals(command.required()));
        item.setEsConfidencial(!Boolean.FALSE.equals(command.confidential()));
        return item;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public OpcionItem createOption(long itemId, OptionCommand command) {
        Item item = find(Item.class, itemId);
        requireDraft(item.getSubtest().getVersionTest().getId());
        OpcionItem option = new OpcionItem();
        option.setItem(item);
        option.setCodigoOpcion(command.code());
        option.setTextoOpcion(command.text());
        option.setNumeroOrden(command.order());
        option.setValorOrdinal(command.ordinalValue());
        option.setEstado(EstadoGeneral.ACTIVO);
        em.persist(option);
        return option;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public OpcionItem updateOption(long optionId, OptionCommand command) {
        OpcionItem option = find(OpcionItem.class, optionId);
        requireDraft(option.getItem().getSubtest().getVersionTest().getId());
        option.setCodigoOpcion(command.code());
        option.setTextoOpcion(command.text());
        option.setNumeroOrden(command.order());
        option.setValorOrdinal(command.ordinalValue());
        return option;
    }

    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR + " or " + SecurityPermissions.TEST_CREAR)
    public List<EstrategiaCalificacion> listStrategies() {
        return em.createQuery("select e from EstrategiaCalificacion e", EstrategiaCalificacion.class).getResultList();
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR)
    public ReglaCalificacion createScoringRule(long subtestId, ScoringRuleCommand command) {
        Subtest subtest = find(Subtest.class, subtestId);
        VersionTest version = requireDraft(subtest.getVersionTest().getId());
        ReglaCalificacion rule = new ReglaCalificacion();
        rule.setVersionTest(version);
        rule.setSubtest(subtest);
        rule.setItem(command.itemId() == null ? null : find(Item.class, command.itemId()));
        rule.setEstrategiaCalificacion(find(EstrategiaCalificacion.class, command.strategyId()));
        rule.setTipoRegla(command.ruleType());
        rule.setPrioridad(command.priority() == null ? 1 : command.priority());
        rule.setActiva(true);
        rule.setEstado(EstadoConfiguracion.BORRADOR);
        rule.setParametros(command.parametersJson() == null ? "{}" : command.parametersJson());
        rule.setObservacion(command.observation());
        rule.setCreadoPor(currentUser());
        rule.setCreadoEn(LocalDateTime.now(clock));
        em.persist(rule);
        return rule;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR)
    public ReglaCalificacion updateScoringRule(long ruleId, ScoringRuleCommand command) {
        ReglaCalificacion rule = find(ReglaCalificacion.class, ruleId);
        requireDraft(rule.getVersionTest().getId());
        rule.setItem(command.itemId() == null ? null : find(Item.class, command.itemId()));
        rule.setEstrategiaCalificacion(find(EstrategiaCalificacion.class, command.strategyId()));
        rule.setTipoRegla(command.ruleType());
        rule.setPrioridad(command.priority() == null ? 1 : command.priority());
        rule.setParametros(command.parametersJson() == null ? "{}" : command.parametersJson());
        rule.setObservacion(command.observation());
        return rule;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR)
    public ClaveRespuesta createAnswerKey(long itemId, AnswerKeyCommand command) {
        Item item = find(Item.class, itemId);
        requireDraft(item.getSubtest().getVersionTest().getId());
        ClaveRespuesta key = new ClaveRespuesta();
        key.setReglaCalificacion(find(ReglaCalificacion.class, command.ruleId()));
        key.setItem(item);
        key.setOpcionCorrecta(command.correctOptionId() == null ? null : find(OpcionItem.class, command.correctOptionId()));
        key.setTextoEsperado(command.expectedText());
        key.setValorNumericoEsperado(command.expectedNumber());
        key.setToleranciaNumerica(command.numericTolerance());
        key.setPuntaje(command.score() == null ? BigDecimal.ONE : command.score());
        key.setRequiereRevisionManual(Boolean.TRUE.equals(command.requiresManualReview()));
        key.setCreadoPor(currentUser());
        key.setCreadoEn(LocalDateTime.now(clock));
        em.persist(key);
        return key;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR)
    public ClaveRespuesta updateAnswerKey(long keyId, AnswerKeyCommand command) {
        ClaveRespuesta key = find(ClaveRespuesta.class, keyId);
        requireDraft(key.getItem().getSubtest().getVersionTest().getId());
        key.setReglaCalificacion(find(ReglaCalificacion.class, command.ruleId()));
        key.setOpcionCorrecta(command.correctOptionId() == null ? null : find(OpcionItem.class, command.correctOptionId()));
        key.setTextoEsperado(command.expectedText());
        key.setValorNumericoEsperado(command.expectedNumber());
        key.setToleranciaNumerica(command.numericTolerance());
        key.setPuntaje(command.score() == null ? BigDecimal.ONE : command.score());
        key.setRequiereRevisionManual(Boolean.TRUE.equals(command.requiresManualReview()));
        return key;
    }

    @PreAuthorize(SecurityPermissions.TEST_LEER)
    public SubtestCloneTemplateDTO getCloneTemplate(long subtestId) {
        Subtest subtest = find(Subtest.class, subtestId);
        List<Item> items = listItems(subtestId);
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        Map<Long, List<OpcionItem>> optionsByItem = optionsByItem(itemIds);
        Map<Long, List<SubtestCloneTemplateDTO.ImageTemplate>> itemImagesByItem = cloneItemImages(itemIds);
        List<Long> optionIds = optionsByItem.values().stream()
                .flatMap(List::stream)
                .map(OpcionItem::getId)
                .toList();
        Map<Long, List<SubtestCloneTemplateDTO.ImageTemplate>> optionImagesByOption = cloneOptionImages(optionIds);
        Map<Long, ClaveRespuesta> keysByItem = answerKeysByItem(itemIds);

        List<SubtestCloneTemplateDTO.ItemTemplate> itemTemplates = items.stream()
                .map(item -> toItemTemplate(item, optionsByItem.getOrDefault(item.getId(), List.of()),
                        itemImagesByItem.getOrDefault(item.getId(), List.of()), optionImagesByOption,
                        keysByItem.get(item.getId())))
                .toList();

        Short strategyId = subtest.getEstrategiaCalificacion() == null
                ? null
                : subtest.getEstrategiaCalificacion().getId();
        return new SubtestCloneTemplateDTO(subtest.getId(), subtest.getCodigoSubtest(), subtest.getNombreSubtest(),
                subtest.getDescripcion(), subtest.getInstrucciones(), subtest.getNumeroOrden(),
                subtest.getTiempoLimiteSegundos(), subtest.getPermiteAleatorizarItems(),
                subtest.getPermiteAleatorizarOpciones(), subtest.getEsObligatorio(), strategyId,
                subtest.getEstado(), itemTemplates);
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public List<Subtest> saveConfiguration(long versionId, VersionConfigurationRequest request) {
        VersionTest version = requireDraft(versionId);
        List<Subtest> saved = new ArrayList<>();
        if (request == null || request.subtests() == null) {
            return saved;
        }
        validateConfigurationRequest(request);
        for (VersionConfigurationRequest.SubtestDraft draft : request.subtests()) {
            if (draft.status() == VersionConfigurationRequest.DraftStatus.DELETED) {
                removeExisting(Subtest.class, draft.id());
                continue;
            }
            Subtest subtest = draft.id() == null ? new Subtest() : find(Subtest.class, draft.id());
            requireSameVersion(versionId, subtest);
            applySubtestDraft(version, subtest, draft);
            if (draft.id() == null) {
                em.persist(subtest);
            }
            persistDraftItems(subtest, nullToEmpty(draft.items()));
            saved.add(subtest);
        }
        return saved;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.BAREMO_CONFIGURAR)
    public Baremo createBaremo(BaremoCommand command) {
        VersionTest version = requireDraft(command.versionId());
        Baremo baremo = new Baremo();
        baremo.setVersionTest(version);
        baremo.setDimensionResultado(command.dimensionId() == null ? null : find(DimensionResultado.class, command.dimensionId()));
        baremo.setCodigoBaremo(command.code());
        baremo.setNombre(command.name());
        baremo.setDescripcion(command.description());
        baremo.setGrupoNormativo(command.normativeGroup());
        baremo.setEstado(EstadoConfiguracion.BORRADOR);
        baremo.setCreadoPor(currentUser());
        baremo.setCreadoEn(LocalDateTime.now(clock));
        em.persist(baremo);
        return baremo;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.BAREMO_CONFIGURAR)
    public Baremo updateBaremo(long baremoId, BaremoCommand command) {
        Baremo baremo = find(Baremo.class, baremoId);
        requireDraft(baremo.getVersionTest().getId());
        baremo.setDimensionResultado(command.dimensionId() == null ? null : find(DimensionResultado.class, command.dimensionId()));
        baremo.setCodigoBaremo(command.code());
        baremo.setNombre(command.name());
        baremo.setDescripcion(command.description());
        baremo.setGrupoNormativo(command.normativeGroup());
        return baremo;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.BAREMO_CONFIGURAR)
    public RangoBaremo createBaremoRange(long baremoId, BaremoRangeCommand command) {
        Baremo baremo = find(Baremo.class, baremoId);
        requireDraft(baremo.getVersionTest().getId());
        RangoBaremo range = new RangoBaremo();
        range.setBaremo(baremo);
        range.setPuntajeMinimo(command.minScore());
        range.setPuntajeMaximo(command.maxScore());
        range.setPercentil(command.percentile());
        range.setCategoria(command.category());
        range.setInterpretacion(command.interpretation());
        range.setRecomendacion(command.recommendation());
        range.setOrden(command.order());
        em.persist(range);
        return range;
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.BAREMO_CONFIGURAR)
    public RangoBaremo updateBaremoRange(long rangeId, BaremoRangeCommand command) {
        RangoBaremo range = find(RangoBaremo.class, rangeId);
        requireDraft(range.getBaremo().getVersionTest().getId());
        range.setPuntajeMinimo(command.minScore());
        range.setPuntajeMaximo(command.maxScore());
        range.setPercentil(command.percentile());
        range.setCategoria(command.category());
        range.setInterpretacion(command.interpretation());
        range.setRecomendacion(command.recommendation());
        range.setOrden(command.order());
        return range;
    }

    private VersionTest requireDraft(long versionId) {
        VersionTest version = versions.findByIdForUpdate(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Version no encontrada: " + versionId));
        if (version.getEstado() != EstadoVersionTest.BORRADOR) {
            throw new IllegalStateException("La version publicada o aprobada no se edita directamente");
        }
        return version;
    }

    @PreAuthorize(SecurityPermissions.TEST_LEER)
    public List<Subtest> listSubtests(long versionId) {
        return em.createQuery("SELECT s FROM Subtest s WHERE s.versionTest.id = :versionId ORDER BY s.numeroOrden ASC", Subtest.class)
                .setParameter("versionId", versionId)
                .getResultList();
    }

    @PreAuthorize(SecurityPermissions.TEST_LEER)
    public List<Item> listItems(long subtestId) {
        return em.createQuery("SELECT i FROM Item i WHERE i.subtest.id = :subtestId ORDER BY i.numeroOrden ASC", Item.class)
                .setParameter("subtestId", subtestId)
                .getResultList();
    }

    @PreAuthorize(SecurityPermissions.TEST_LEER)
    public List<ItemDTO> listItemDtos(long subtestId) {
        List<Item> items = listItems(subtestId);
        Map<Long, List<ImageResourceDTO>> imagesByItem = itemImages(items.stream().map(Item::getId).toList());
        return items.stream()
                .map(item -> ItemDTO.from(item, imagesByItem.getOrDefault(item.getId(), List.of())))
                .toList();
    }

    @PreAuthorize(SecurityPermissions.TEST_LEER)
    public List<OpcionItem> listOptions(long itemId) {
        return em.createQuery("SELECT o FROM OpcionItem o WHERE o.item.id = :itemId ORDER BY o.numeroOrden ASC", OpcionItem.class)
                .setParameter("itemId", itemId)
                .getResultList();
    }

    @PreAuthorize(SecurityPermissions.TEST_LEER)
    public List<OptionDTO> listOptionDtos(long itemId) {
        List<OpcionItem> options = listOptions(itemId);
        Map<Long, List<ImageResourceDTO>> imagesByOption = optionImages(options.stream().map(OpcionItem::getId).toList());
        return options.stream()
                .map(option -> OptionDTO.from(option, imagesByOption.getOrDefault(option.getId(), List.of())))
                .toList();
    }

    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR + " or " + SecurityPermissions.TEST_LEER)
    public List<ReglaCalificacion> listScoringRules(long subtestId) {
        return em.createQuery("SELECT r FROM ReglaCalificacion r WHERE r.subtest.id = :subtestId ORDER BY r.prioridad ASC", ReglaCalificacion.class)
                .setParameter("subtestId", subtestId)
                .getResultList();
    }

    @PreAuthorize(SecurityPermissions.CALIFICACION_CONFIGURAR + " or " + SecurityPermissions.TEST_LEER)
    public ClaveRespuesta findAnswerKey(long itemId) {
        List<ClaveRespuesta> keys = em.createQuery("SELECT c FROM ClaveRespuesta c WHERE c.item.id = :itemId", ClaveRespuesta.class)
                .setParameter("itemId", itemId)
                .setMaxResults(1)
                .getResultList();
        return keys.isEmpty() ? null : keys.get(0);
    }

    private void validateConfigurationRequest(VersionConfigurationRequest request) {
        Set<String> subtestCodes = new HashSet<>();
        Set<Integer> subtestOrders = new HashSet<>();
        for (VersionConfigurationRequest.SubtestDraft subtest : request.subtests()) {
            if (subtest.status() == VersionConfigurationRequest.DraftStatus.DELETED) {
                continue;
            }
            if (!subtestCodes.add(normalized(subtest.code()))) {
                throw new IllegalArgumentException("Codigo de subtest duplicado: " + subtest.code());
            }
            if (subtest.order() != null && !subtestOrders.add(subtest.order())) {
                throw new IllegalArgumentException("Orden de subtest duplicado: " + subtest.order());
            }
            validateItemDrafts(nullToEmpty(subtest.items()));
        }
    }

    private void validateItemDrafts(List<VersionConfigurationRequest.ItemDraft> items) {
        Set<String> itemCodes = new HashSet<>();
        Set<Integer> itemOrders = new HashSet<>();
        for (VersionConfigurationRequest.ItemDraft item : items) {
            if (item.status() == VersionConfigurationRequest.DraftStatus.DELETED) {
                continue;
            }
            if (!itemCodes.add(normalized(item.code()))) {
                throw new IllegalArgumentException("Codigo de item duplicado: " + item.code());
            }
            if (item.order() != null && !itemOrders.add(item.order())) {
                throw new IllegalArgumentException("Orden de item duplicado: " + item.order());
            }
            validateOptionDrafts(nullToEmpty(item.options()));
        }
    }

    private void validateOptionDrafts(List<VersionConfigurationRequest.OptionDraft> options) {
        Set<String> optionCodes = new HashSet<>();
        Set<Integer> optionOrders = new HashSet<>();
        for (VersionConfigurationRequest.OptionDraft option : options) {
            if (option.status() == VersionConfigurationRequest.DraftStatus.DELETED) {
                continue;
            }
            if (!optionCodes.add(normalized(option.code()))) {
                throw new IllegalArgumentException("Codigo de opcion duplicado: " + option.code());
            }
            if (option.order() != null && !optionOrders.add(option.order())) {
                throw new IllegalArgumentException("Orden de opcion duplicado: " + option.order());
            }
        }
    }

    private static String normalized(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private SubtestCloneTemplateDTO.ItemTemplate toItemTemplate(Item item, List<OpcionItem> options,
            List<SubtestCloneTemplateDTO.ImageTemplate> images,
            Map<Long, List<SubtestCloneTemplateDTO.ImageTemplate>> optionImagesByOption, ClaveRespuesta key) {
        List<SubtestCloneTemplateDTO.OptionTemplate> optionTemplates = options.stream()
                .map(option -> new SubtestCloneTemplateDTO.OptionTemplate(option.getId(), option.getCodigoOpcion(),
                        option.getTextoOpcion(), option.getNumeroOrden(), option.getValorOrdinal(),
                        option.getEstado(), optionImagesByOption.getOrDefault(option.getId(), List.of())))
                .toList();
        SubtestCloneTemplateDTO.AnswerKeyTemplate answerKey = key == null ? null
                : new SubtestCloneTemplateDTO.AnswerKeyTemplate(key.getId(),
                        key.getReglaCalificacion() == null ? null : key.getReglaCalificacion().getId(),
                        key.getOpcionCorrecta() == null ? null : key.getOpcionCorrecta().getId(),
                        key.getTextoEsperado(), key.getValorNumericoEsperado(), key.getToleranciaNumerica(),
                        key.getPuntaje(), key.getRequiereRevisionManual());
        return new SubtestCloneTemplateDTO.ItemTemplate(item.getId(), item.getCodigoItem(), item.getTipoItem(),
                item.getTipoRespuesta(), item.getEnunciado(), item.getInstruccion(), item.getNumeroOrden(),
                item.getPuntajeBase(), item.getTiempoLimiteSegundos(), item.getEsObligatorio(),
                item.getEsConfidencial(), item.getEstado(), images, optionTemplates, answerKey);
    }

    private Map<Long, List<OpcionItem>> optionsByItem(List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Map.of();
        }
        List<OpcionItem> options = em.createQuery("""
                SELECT o FROM OpcionItem o
                WHERE o.item.id IN :itemIds
                ORDER BY o.item.id ASC, o.numeroOrden ASC
                """, OpcionItem.class)
                .setParameter("itemIds", itemIds)
                .getResultList();
        Map<Long, List<OpcionItem>> result = new LinkedHashMap<>();
        for (OpcionItem option : options) {
            result.computeIfAbsent(option.getItem().getId(), ignored -> new ArrayList<>()).add(option);
        }
        return result;
    }

    private Map<Long, ClaveRespuesta> answerKeysByItem(List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Map.of();
        }
        List<ClaveRespuesta> keys = em.createQuery("""
                SELECT c FROM ClaveRespuesta c
                LEFT JOIN FETCH c.opcionCorrecta
                WHERE c.item.id IN :itemIds
                """, ClaveRespuesta.class)
                .setParameter("itemIds", itemIds)
                .getResultList();
        Map<Long, ClaveRespuesta> result = new LinkedHashMap<>();
        for (ClaveRespuesta key : keys) {
            result.putIfAbsent(key.getItem().getId(), key);
        }
        return result;
    }

    private void applySubtestDraft(VersionTest version, Subtest subtest, VersionConfigurationRequest.SubtestDraft draft) {
        subtest.setVersionTest(version);
        subtest.setEstrategiaCalificacion(draft.strategyId() == null ? null : find(EstrategiaCalificacion.class, draft.strategyId()));
        subtest.setCodigoSubtest(draft.code());
        subtest.setNombreSubtest(draft.name());
        subtest.setDescripcion(draft.description());
        subtest.setInstrucciones(draft.instructions());
        subtest.setNumeroOrden(draft.order());
        subtest.setTiempoLimiteSegundos(draft.timeLimitSeconds());
        boolean randomizeItems = Boolean.TRUE.equals(draft.randomizeItems());
        if (randomizeItems && !Boolean.TRUE.equals(version.getPermiteAleatorizarItems())) {
            randomizeItems = false;
        }
        subtest.setPermiteAleatorizarItems(randomizeItems);
        subtest.setPermiteAleatorizarOpciones(Boolean.TRUE.equals(draft.randomizeOptions()));
        subtest.setEsObligatorio(!Boolean.FALSE.equals(draft.required()));
        subtest.setEstado(EstadoGeneral.ACTIVO);
    }

    private void persistDraftItems(Subtest subtest, List<VersionConfigurationRequest.ItemDraft> drafts) {
        Map<String, OpcionItem> optionsByDraftId = new HashMap<>();
        List<PendingAnswerKey> pendingKeys = new ArrayList<>();
        for (VersionConfigurationRequest.ItemDraft draft : drafts) {
            if (draft.status() == VersionConfigurationRequest.DraftStatus.DELETED) {
                removeExisting(Item.class, draft.id());
                continue;
            }
            Item item = draft.id() == null ? new Item() : find(Item.class, draft.id());
            item.setSubtest(subtest);
            item.setCodigoItem(draft.code());
            item.setTipoItem(draft.itemType());
            item.setTipoRespuesta(draft.responseType());
            item.setEnunciado(draft.prompt());
            item.setInstruccion(draft.instruction());
            item.setNumeroOrden(draft.order());
            item.setPuntajeBase(draft.baseScore() == null ? BigDecimal.ONE : draft.baseScore());
            item.setTiempoLimiteSegundos(draft.timeLimitSeconds());
            item.setEsObligatorio(!Boolean.FALSE.equals(draft.required()));
            item.setEsConfidencial(!Boolean.FALSE.equals(draft.confidential()));
            item.setEstado(EstadoGeneral.ACTIVO);
            if (draft.id() == null) {
                em.persist(item);
            }
            persistItemImages(item, nullToEmpty(draft.images()));
            persistDraftOptions(item, nullToEmpty(draft.options()), optionsByDraftId);
            if (draft.answerKey() != null && draft.answerKey().status() != VersionConfigurationRequest.DraftStatus.DELETED) {
                pendingKeys.add(new PendingAnswerKey(item, draft.answerKey()));
            }
        }
        for (PendingAnswerKey pendingKey : pendingKeys) {
            persistAnswerKey(pendingKey.item(), pendingKey.draft(), optionsByDraftId);
        }
    }

    private void persistDraftOptions(Item item, List<VersionConfigurationRequest.OptionDraft> drafts,
            Map<String, OpcionItem> optionsByDraftId) {
        for (VersionConfigurationRequest.OptionDraft draft : drafts) {
            if (draft.status() == VersionConfigurationRequest.DraftStatus.DELETED) {
                removeExisting(OpcionItem.class, draft.id());
                continue;
            }
            OpcionItem option = draft.id() == null ? new OpcionItem() : find(OpcionItem.class, draft.id());
            option.setItem(item);
            option.setCodigoOpcion(draft.code());
            option.setTextoOpcion(draft.text());
            option.setNumeroOrden(draft.order());
            option.setValorOrdinal(draft.ordinalValue());
            option.setEstado(EstadoGeneral.ACTIVO);
            if (draft.id() == null) {
                em.persist(option);
            }
            if (draft.draftId() != null) {
                optionsByDraftId.put(draft.draftId(), option);
            }
            persistOptionImages(option, nullToEmpty(draft.images()));
        }
    }

    private void persistItemImages(Item item, List<VersionConfigurationRequest.ImageDraft> drafts) {
        for (VersionConfigurationRequest.ImageDraft draft : drafts) {
            if (draft.status() == VersionConfigurationRequest.DraftStatus.DELETED) {
                removeExisting(ImagenItem.class, draft.id());
                continue;
            }
            ImagenItem image = draft.id() == null ? new ImagenItem() : find(ImagenItem.class, draft.id());
            image.setItem(item);
            image.setRecurso(find(RecursoMultimedia.class, draft.resourceId()));
            image.setNumeroOrden(draft.order());
            image.setTextoAlternativo(draft.altText());
            image.setRolImagen(draft.role() == null ? "ENUNCIADO" : draft.role());
            if (draft.id() == null) {
                em.persist(image);
            }
        }
    }

    private void persistOptionImages(OpcionItem option, List<VersionConfigurationRequest.ImageDraft> drafts) {
        for (VersionConfigurationRequest.ImageDraft draft : drafts) {
            if (draft.status() == VersionConfigurationRequest.DraftStatus.DELETED) {
                removeExisting(ImagenOpcion.class, draft.id());
                continue;
            }
            ImagenOpcion image = draft.id() == null ? new ImagenOpcion() : find(ImagenOpcion.class, draft.id());
            image.setOpcion(option);
            image.setRecurso(find(RecursoMultimedia.class, draft.resourceId()));
            image.setNumeroOrden(draft.order());
            image.setTextoAlternativo(draft.altText());
            if (draft.id() == null) {
                em.persist(image);
            }
        }
    }

    private void persistAnswerKey(Item item, VersionConfigurationRequest.AnswerKeyDraft draft,
            Map<String, OpcionItem> optionsByDraftId) {
        ClaveRespuesta key = draft.id() == null ? new ClaveRespuesta() : find(ClaveRespuesta.class, draft.id());
        key.setReglaCalificacion(find(ReglaCalificacion.class, draft.ruleId()));
        key.setItem(item);
        key.setOpcionCorrecta(resolveCorrectOption(draft, optionsByDraftId));
        key.setTextoEsperado(draft.expectedText());
        key.setValorNumericoEsperado(draft.expectedNumber());
        key.setToleranciaNumerica(draft.numericTolerance());
        key.setPuntaje(draft.score() == null ? BigDecimal.ONE : draft.score());
        key.setRequiereRevisionManual(Boolean.TRUE.equals(draft.requiresManualReview()));
        if (draft.id() == null) {
            key.setCreadoPor(currentUser());
            key.setCreadoEn(LocalDateTime.now(clock));
            em.persist(key);
        }
    }

    private OpcionItem resolveCorrectOption(VersionConfigurationRequest.AnswerKeyDraft draft,
            Map<String, OpcionItem> optionsByDraftId) {
        if (draft.correctOptionDraftId() != null) {
            OpcionItem option = optionsByDraftId.get(draft.correctOptionDraftId());
            if (option == null) {
                throw new EntityNotFoundException("Opcion draft no encontrada: " + draft.correctOptionDraftId());
            }
            return option;
        }
        return draft.correctOptionId() == null ? null : find(OpcionItem.class, draft.correctOptionId());
    }

    private void requireSameVersion(long versionId, Subtest subtest) {
        if (subtest.getId() != null && !Objects.equals(subtest.getVersionTest().getId(), versionId)) {
            throw new IllegalArgumentException("El subtest no pertenece a la version destino");
        }
    }

    private <T> void removeExisting(Class<T> type, Long id) {
        if (id == null) {
            return;
        }
        em.remove(find(type, id));
    }

    private static <T> List<T> nullToEmpty(List<T> values) {
        return values == null ? List.of() : values;
    }

    @PreAuthorize(SecurityPermissions.BAREMO_CONFIGURAR + " or " + SecurityPermissions.TEST_LEER)
    public List<Baremo> listBaremos(long versionId) {
        return em.createQuery("SELECT b FROM Baremo b WHERE b.versionTest.id = :versionId ORDER BY b.codigoBaremo ASC", Baremo.class)
                .setParameter("versionId", versionId)
                .getResultList();
    }

    @PreAuthorize(SecurityPermissions.BAREMO_CONFIGURAR + " or " + SecurityPermissions.TEST_LEER)
    public List<RangoBaremo> listBaremoRanges(long baremoId) {
        return em.createQuery("SELECT r FROM RangoBaremo r WHERE r.baremo.id = :baremoId ORDER BY r.orden ASC", RangoBaremo.class)
                .setParameter("baremoId", baremoId)
                .getResultList();
    }

    private Usuario currentUser() {
        UUID id = currentActor.usuarioId();
        return users.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado: " + id));
    }

    private <T> T find(Class<T> type, Object id) {
        T value = em.find(type, id);
        if (value == null) {
            throw new EntityNotFoundException(type.getSimpleName() + " no encontrado: " + id);
        }
        return value;
    }

    private Map<Long, List<ImageResourceDTO>> itemImages(List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> rows = em.createQuery("""
                SELECT img.item.id, img.id, img.numeroOrden, img.textoAlternativo, res.rutaAlmacenamiento,
                       img.rolImagen
                FROM ImagenItem img
                JOIN img.recurso res
                WHERE img.item.id IN :itemIds
                ORDER BY img.item.id ASC, img.numeroOrden ASC
                """, Object[].class)
                .setParameter("itemIds", itemIds)
                .getResultList();
        Map<Long, List<ImageResourceDTO>> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Long ownerId = (Long) row[0];
            result.computeIfAbsent(ownerId, ignored -> new ArrayList<>())
                    .add(new ImageResourceDTO((Long) row[1], (Integer) row[2], (String) row[3], (String) row[4],
                            null, (String) row[5]));
        }
        return result;
    }

    private Map<Long, List<ImageResourceDTO>> optionImages(List<Long> optionIds) {
        if (optionIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> rows = em.createQuery("""
                SELECT img.opcion.id, img.id, img.numeroOrden, img.textoAlternativo, res.rutaAlmacenamiento
                FROM ImagenOpcion img
                JOIN img.recurso res
                WHERE img.opcion.id IN :optionIds
                ORDER BY img.opcion.id ASC, img.numeroOrden ASC
                """, Object[].class)
                .setParameter("optionIds", optionIds)
                .getResultList();
        Map<Long, List<ImageResourceDTO>> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Long ownerId = (Long) row[0];
            result.computeIfAbsent(ownerId, ignored -> new ArrayList<>())
                    .add(new ImageResourceDTO((Long) row[1], (Integer) row[2], (String) row[3], (String) row[4],
                            null, null));
        }
        return result;
    }

    private Map<Long, List<SubtestCloneTemplateDTO.ImageTemplate>> cloneItemImages(List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> rows = em.createQuery("""
                SELECT img.item.id, img.id, img.recurso.id, img.numeroOrden, img.textoAlternativo,
                       res.rutaAlmacenamiento, img.rolImagen
                FROM ImagenItem img
                JOIN img.recurso res
                WHERE img.item.id IN :itemIds
                ORDER BY img.item.id ASC, img.numeroOrden ASC
                """, Object[].class)
                .setParameter("itemIds", itemIds)
                .getResultList();
        Map<Long, List<SubtestCloneTemplateDTO.ImageTemplate>> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Long ownerId = (Long) row[0];
            result.computeIfAbsent(ownerId, ignored -> new ArrayList<>())
                    .add(new SubtestCloneTemplateDTO.ImageTemplate((Long) row[1], (Long) row[2],
                            (Integer) row[3], (String) row[4], (String) row[5], (String) row[6]));
        }
        return result;
    }

    private Map<Long, List<SubtestCloneTemplateDTO.ImageTemplate>> cloneOptionImages(List<Long> optionIds) {
        if (optionIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> rows = em.createQuery("""
                SELECT img.opcion.id, img.id, img.recurso.id, img.numeroOrden, img.textoAlternativo,
                       res.rutaAlmacenamiento
                FROM ImagenOpcion img
                JOIN img.recurso res
                WHERE img.opcion.id IN :optionIds
                ORDER BY img.opcion.id ASC, img.numeroOrden ASC
                """, Object[].class)
                .setParameter("optionIds", optionIds)
                .getResultList();
        Map<Long, List<SubtestCloneTemplateDTO.ImageTemplate>> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Long ownerId = (Long) row[0];
            result.computeIfAbsent(ownerId, ignored -> new ArrayList<>())
                    .add(new SubtestCloneTemplateDTO.ImageTemplate((Long) row[1], (Long) row[2],
                            (Integer) row[3], (String) row[4], (String) row[5], null));
        }
        return result;
    }

    public record TestCommand(String code, String name, String description) {
    }

    public record VersionCommand(String number, Short strategyId, String instructions, Integer timeLimitSeconds,
            Boolean randomizeSubtests, Boolean randomizeItems) {
    }

    public record SubtestCommand(String code, String name, String description, String instructions, Integer order,
            Integer timeLimitSeconds, Boolean randomizeItems, Boolean randomizeOptions, Boolean required,
            Short strategyId) {
    }

    public record ItemCommand(String code, TipoItem itemType, TipoRespuesta responseType, String prompt,
            String instruction, Integer order, BigDecimal baseScore, Integer timeLimitSeconds, Boolean required,
            Boolean confidential) {
    }

    public record OptionCommand(String code, String text, Integer order, BigDecimal ordinalValue) {
    }

    public record ScoringRuleCommand(Short strategyId, TipoReglaCalificacion ruleType, Long itemId, Integer priority,
            String parametersJson, String observation) {
    }

    public record AnswerKeyCommand(Long ruleId, Long correctOptionId, String expectedText, BigDecimal expectedNumber,
            BigDecimal numericTolerance, BigDecimal score, Boolean requiresManualReview) {
    }

    public record BaremoCommand(Long versionId, Long dimensionId, String code, String name, String description,
            String normativeGroup) {
    }

    public record BaremoRangeCommand(BigDecimal minScore, BigDecimal maxScore, BigDecimal percentile, String category,
            String interpretation, String recommendation, Integer order) {
    }

    private record PendingAnswerKey(Item item, VersionConfigurationRequest.AnswerKeyDraft draft) {
    }
}
