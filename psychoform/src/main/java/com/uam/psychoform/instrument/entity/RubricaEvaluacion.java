package com.uam.psychoform.instrument.entity;

import com.uam.psychoform.security.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "rubrica_evaluacion")
public class RubricaEvaluacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rubrica_id")
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
    @NotBlank
    @Size(max = 60)
    @Column(name = "codigo_rubrica", nullable = false, length = 60)
    private String codigoRubrica;
    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(columnDefinition = "text")
    private String descripcion;
    @DecimalMin("0")
    @Column(name = "puntaje_maximo", precision = 8, scale = 2)
    private BigDecimal puntajeMaximo;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_configuracion")
    private EstadoConfiguracion estado;
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
