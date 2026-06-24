package com.uam.psychoform.instrument.entity;

import com.uam.psychoform.security.entity.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "regla_calificacion")
public class ReglaCalificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "regla_calificacion_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_test_id", nullable = false)
    private VersionTest versionTest;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtest_id")
    private Subtest subtest;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estrategia_calificacion_id", nullable = false)
    private EstrategiaCalificacion estrategiaCalificacion;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tipo_regla", nullable = false, columnDefinition = "tipo_regla_calificacion")
    private TipoReglaCalificacion tipoRegla;
    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer prioridad;
    @NotNull
    @Column(nullable = false)
    private Boolean activa;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_configuracion")
    private EstadoConfiguracion estado;
    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String parametros;
    @Column(columnDefinition = "text")
    private String observacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;
    @NotNull
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por")
    private Usuario aprobadoPor;
    @Column(name = "aprobado_en")
    private LocalDateTime aprobadoEn;
}
