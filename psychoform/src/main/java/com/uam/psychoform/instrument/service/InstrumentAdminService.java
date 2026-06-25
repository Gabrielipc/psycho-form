package com.uam.psychoform.instrument.service;

import com.uam.psychoform.instrument.model.*;
import com.uam.psychoform.instrument.repository.VersionTestRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.model.EstadoGeneral;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
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

    @PreAuthorize("hasAuthority('PERM_TEST_CREAR') or hasAuthority('PERM_TEST_PUBLICAR')")
    public List<TestPsicologico> listTests() {
        return em.createQuery("select t from TestPsicologico t", TestPsicologico.class).getResultList();
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_TEST_CREAR')")
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

    @PreAuthorize("hasAuthority('PERM_TEST_CREAR') or hasAuthority('PERM_TEST_PUBLICAR')")
    public List<VersionTest> listVersions(long testId) {
        return em.createQuery("select v from VersionTest v where v.test.id = :testId", VersionTest.class)
                .setParameter("testId", testId).getResultList();
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_TEST_CREAR')")
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
    @PreAuthorize("hasAuthority('PERM_TEST_CREAR')")
    public VersionTest updateVersion(long versionId, VersionCommand command) {
        VersionTest version = requireDraft(versionId);
        version.setEstrategiaCalificacion(command.strategyId() == null ? version.getEstrategiaCalificacion()
                : find(EstrategiaCalificacion.class, command.strategyId()));
        version.setInstruccionesGenerales(command.instructions());
        version.setTiempoLimiteSegundos(command.timeLimitSeconds());
        version.setPermiteAleatorizarItems(Boolean.TRUE.equals(command.randomizeItems()));
        version.setPermiteAleatorizarSubtests(Boolean.TRUE.equals(command.randomizeSubtests()));
        return version;
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_TEST_CREAR')")
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
        subtest.setPermiteAleatorizarItems(Boolean.TRUE.equals(command.randomizeItems()));
        subtest.setPermiteAleatorizarOpciones(Boolean.TRUE.equals(command.randomizeOptions()));
        subtest.setEsObligatorio(!Boolean.FALSE.equals(command.required()));
        subtest.setEstado(EstadoGeneral.ACTIVO);
        em.persist(subtest);
        return subtest;
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_TEST_CREAR')")
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
    @PreAuthorize("hasAuthority('PERM_TEST_CREAR')")
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

    @PreAuthorize("hasAuthority('PERM_CALIFICACION_CONFIGURAR') or hasAuthority('PERM_TEST_CREAR')")
    public List<EstrategiaCalificacion> listStrategies() {
        return em.createQuery("select e from EstrategiaCalificacion e", EstrategiaCalificacion.class).getResultList();
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_CALIFICACION_CONFIGURAR')")
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
    @PreAuthorize("hasAuthority('PERM_CALIFICACION_CONFIGURAR')")
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
    @PreAuthorize("hasAuthority('PERM_BAREMO_CONFIGURAR')")
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
    @PreAuthorize("hasAuthority('PERM_BAREMO_CONFIGURAR')")
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

    private VersionTest requireDraft(long versionId) {
        VersionTest version = versions.findByIdForUpdate(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Version no encontrada: " + versionId));
        if (version.getEstado() != EstadoVersionTest.BORRADOR) {
            throw new IllegalStateException("La version publicada o aprobada no se edita directamente");
        }
        return version;
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
}
